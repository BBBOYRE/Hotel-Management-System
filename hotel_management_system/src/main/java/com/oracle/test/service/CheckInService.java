package com.oracle.test.service;

import com.oracle.test.common.BusinessException;
import com.oracle.test.common.PageResult;
import com.oracle.test.entity.*;
import com.oracle.test.mapper.*;
import com.oracle.test.util.OrderNoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class CheckInService {

    @Autowired private CheckInRecordMapper checkMapper;
    @Autowired private ReservationMapper resMapper;
    @Autowired private RoomMapper roomMapper;
    @Autowired private RoomTypeMapper roomTypeMapper;
    @Autowired private HotelOrderMapper orderMapper;
    @Autowired private GuestRecordMapper guestMapper;
    @Autowired private BillService billService;
    @Autowired private SystemClock clock;

    public PageResult<CheckInRecord> page(String keyword, Integer status,
                                          Date startDate, Date endDate,
                                          int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<CheckInRecord> records = checkMapper.search(keyword, status, startDate, endDate, offset, pageSize);
        long total = checkMapper.count(keyword, status, startDate, endDate);
        return new PageResult<>(total, records, pageNum, pageSize);
    }

    public CheckInRecord get(Long recordId) {
        return checkMapper.findById(recordId);
    }

    public List<CheckInRecord> listByOrder(Long orderId) {
        return checkMapper.listByOrder(orderId);
    }

    /**
     * 入住办理：可来源于预订（resId 非空）也可步入式（resId 为空 + customerId 非空）。
     * 通过 ROOM 表乐观锁防止一房多卖。
     */
    @Transactional
    public CheckInRecord checkIn(Long resId, Long roomId, Long customerId,
                                 List<GuestRecord> guests, Long updateBy) {
        Room room = roomMapper.findById(roomId);
        if (room == null) throw new BusinessException("房间不存在");
        room.assertCanCheckIn();

        Long orderId;
        if (resId != null) {
            Reservation res = resMapper.findById(resId);
            if (res == null) throw new BusinessException("预订不存在");
            if (!res.isPending()) throw new BusinessException("预订状态不可入住");
            if (!res.getTypeId().equals(room.getTypeId())) {
                throw new BusinessException("分配的房间类型与预订房型不一致");
            }
            int u = resMapper.updateStatusWithVersion(resId, Reservation.STATUS_CHECKED, res.getVersion(), updateBy);
            if (u == 0) throw new BusinessException("预订已被他人修改，请刷新后重试");
            orderId = res.getOrderId();
        } else {
            if (customerId == null) throw new BusinessException("步入式入住必须指定客户");
            HotelOrder order = new HotelOrder();
            order.setOrderNo(OrderNoUtil.generate("WALK"));
            order.setCustomerId(customerId);
            order.setStatus(HotelOrder.STATUS_ONGOING);
            order.setUpdateBy(updateBy);
            orderMapper.insert(order);
            orderId = order.getOrderId();
        }

        int rows = roomMapper.updateStatusWithVersion(roomId, Room.STATUS_OCCUPIED, room.getVersion(), updateBy);
        if (rows == 0) throw new BusinessException("房间已被他人占用，请重新选择");

        CheckInRecord record = new CheckInRecord();
        record.setOrderId(orderId);
        record.setResId(resId);
        record.setRoomId(roomId);
        record.setCheckIn(clock.now());
        record.setStatus(CheckInRecord.STATUS_IN);
        record.setUpdateBy(updateBy);
        checkMapper.insert(record);

        if (guests != null) {
            for (GuestRecord g : guests) {
                if (g.getGuestName() == null || g.getIdCard() == null) continue;
                g.setRecordId(record.getRecordId());
                g.setUpdateBy(updateBy);
                guestMapper.insert(g);
            }
        }

        return record;
    }

    /**
     * 退房：变更入住状态、房间转空闲脏房，并按订单的所有入住段累计生成房费账单。
     * 一次完整入住（含若干次换房）只在最终退房时结算，避免重复出账。
     */
    @Transactional
    public void checkOut(Long recordId, Long updateBy) {
        CheckInRecord record = checkMapper.findById(recordId);
        if (record == null) throw new BusinessException("入住记录不存在");
        if (!record.isInHouse()) throw new BusinessException("该记录已退房或异常");

        int u = checkMapper.updateCheckOutWithVersion(recordId, record.getVersion(), updateBy);
        if (u == 0) throw new BusinessException("入住记录已被他人修改，请刷新后重试");

        Room room = roomMapper.findById(record.getRoomId());
        if (room != null) {
            roomMapper.updateStatusWithVersion(record.getRoomId(), Room.STATUS_FREE_DIRTY, room.getVersion(), updateBy);
        }

        // 按订单累计所有段的房费：每段 nights * 该段所在房型的 basePrice
        Long orderId = record.getOrderId();
        List<CheckInRecord> segments = checkMapper.listByOrder(orderId);
        int idx = 0;
        for (CheckInRecord seg : segments) {
            idx++;
            Room segRoom = roomMapper.findById(seg.getRoomId());
            if (segRoom == null) continue;
            RoomType type = roomTypeMapper.findById(segRoom.getTypeId());
            if (type == null) continue;
            int nights = seg.nights();
            BillItem fee = new BillItem();
            fee.setOrderId(orderId);
            fee.setSegmentId(seg.getRecordId());
            fee.setItemType(BillItem.TYPE_ROOM_FEE);
            fee.setAmount(type.calcAmount(nights));
            String prefix = segments.size() > 1 ? ("第" + idx + "段：") : "";
            fee.setRemark(prefix + "房间 " + segRoom.getRoomNo() + " 共住 " + nights + " 晚");
            billService.addItem(fee, updateBy);
        }
    }

    /**
     * 换房：旧房标脏 + 关闭旧入住段 + 新房入住段开启。
     * 注意：换房时不立即出账，所有段的房费在最终退房时统一累计，避免同一晚被收两次。
     */
    @Transactional
    public void changeRoom(Long recordId, Long newRoomId, Long updateBy) {
        CheckInRecord record = checkMapper.findById(recordId);
        if (record == null) throw new BusinessException("入住记录不存在");
        if (!record.isInHouse()) throw new BusinessException("仅在住记录可换房");

        Room newRoom = roomMapper.findById(newRoomId);
        if (newRoom == null) throw new BusinessException("新房间不存在");
        newRoom.assertCanCheckIn();

        Room oldRoom = roomMapper.findById(record.getRoomId());
        if (oldRoom != null) {
            roomMapper.updateStatusWithVersion(oldRoom.getRoomId(), Room.STATUS_FREE_DIRTY, oldRoom.getVersion(), updateBy);
        }
        int rows = roomMapper.updateStatusWithVersion(newRoomId, Room.STATUS_OCCUPIED, newRoom.getVersion(), updateBy);
        if (rows == 0) throw new BusinessException("新房间已被他人占用");

        int u = checkMapper.updateCheckOutWithVersion(recordId, record.getVersion(), updateBy);
        if (u == 0) throw new BusinessException("旧入住记录修改失败");

        CheckInRecord nr = new CheckInRecord();
        nr.setOrderId(record.getOrderId());
        nr.setResId(record.getResId());
        nr.setRoomId(newRoomId);
        nr.setCheckIn(clock.now());
        nr.setStatus(CheckInRecord.STATUS_IN);
        nr.setUpdateBy(updateBy);
        checkMapper.insert(nr);
    }

    public long countInHouse() {
        return checkMapper.countInHouse();
    }
}
