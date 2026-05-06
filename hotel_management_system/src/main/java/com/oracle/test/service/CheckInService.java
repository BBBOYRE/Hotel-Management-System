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
    @Autowired private BillItemMapper billMapper;

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
        record.setCheckIn(new Date());
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

    /** 退房：变更入住状态，房间转空闲脏房，按入住夜数生成房费账单。 */
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

        // 自动生成房费账单（如果还没生成）
        record = checkMapper.findById(recordId);
        int nights = record.nights();
        RoomType type = roomTypeMapper.findById(room == null ? null : room.getTypeId());
        if (type != null) {
            BillItem fee = new BillItem();
            fee.setOrderId(record.getOrderId());
            fee.setItemType(BillItem.TYPE_ROOM_FEE);
            fee.setAmount(type.calcAmount(nights));
            fee.setRemark("房间 " + room.getRoomNo() + " 共住 " + nights + " 晚");
            fee.setOperatorId(updateBy);
            billMapper.insert(fee);
        }
    }

    /** 异常换房：旧房标脏 + 新房入住 */
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

        // 直接更新入住记录的 roomId（通过 update 即可，简化为：插入一条新记录 + 关闭旧记录）
        int u = checkMapper.updateCheckOutWithVersion(recordId, record.getVersion(), updateBy);
        if (u == 0) throw new BusinessException("旧入住记录修改失败");

        CheckInRecord nr = new CheckInRecord();
        nr.setOrderId(record.getOrderId());
        nr.setResId(record.getResId());
        nr.setRoomId(newRoomId);
        nr.setCheckIn(new Date());
        nr.setStatus(CheckInRecord.STATUS_IN);
        nr.setUpdateBy(updateBy);
        checkMapper.insert(nr);
    }

    public long countInHouse() {
        return checkMapper.countInHouse();
    }
}
