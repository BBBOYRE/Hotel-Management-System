package com.oracle.test.service;

import com.oracle.test.common.BusinessException;
import com.oracle.test.common.PageResult;
import com.oracle.test.entity.Room;
import com.oracle.test.entity.RoomService;
import com.oracle.test.mapper.RoomMapper;
import com.oracle.test.mapper.RoomServiceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HousekeepingService {

    @Autowired private RoomServiceMapper serviceMapper;
    @Autowired private RoomMapper roomMapper;
    @Autowired private com.oracle.test.service.RoomService roomService;

    public PageResult<RoomService> page(Integer status, Integer serviceType, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<RoomService> records = serviceMapper.search(status, serviceType, offset, pageSize);
        long total = serviceMapper.count(status, serviceType);
        return new PageResult<>(total, records, pageNum, pageSize);
    }

    @Transactional
    public Long createOrder(RoomService req, Long updateBy) {
        Room room = roomMapper.findById(req.getRoomId());
        if (room == null) throw new BusinessException("房间不存在");
        if (req.getServiceType() == null) throw new BusinessException("请选择工单类型");
        // 派单时校验类型与房态匹配：清扫只对应空闲脏房，维修只对应维修中（房态非维修中时由派单触发置为维修中）
        if (req.getServiceType() == RoomService.TYPE_CLEAN
                && room.getStatus() != null && room.getStatus() != Room.STATUS_FREE_DIRTY) {
            throw new BusinessException("仅对空闲脏房可派清扫工单");
        }
        if (req.getServiceType() == RoomService.TYPE_REPAIR
                && room.getStatus() != null
                && room.getStatus() != Room.STATUS_FREE_CLEAN
                && room.getStatus() != Room.STATUS_FREE_DIRTY
                && room.getStatus() != Room.STATUS_REPAIRING) {
            throw new BusinessException("已入住的房间不能直接派维修，请先处理退房");
        }
        req.setUpdateBy(updateBy);
        if (req.getStatus() == null) req.setStatus(RoomService.STATUS_PENDING);
        serviceMapper.insert(req);
        if (req.getServiceType() == RoomService.TYPE_REPAIR
                && room.getStatus() != Room.STATUS_REPAIRING) {
            roomService.updateStatus(room.getRoomId(), Room.STATUS_REPAIRING, updateBy);
        }
        return req.getServiceId();
    }

    @Transactional
    public void take(Long serviceId, Long operatorId, Long updateBy) {
        serviceMapper.updateStatus(serviceId, RoomService.STATUS_DOING, operatorId, updateBy);
    }

    /**
     * 完成工单：
     * - 清扫工单（TYPE_CLEAN）：仅在房间为「空闲脏房」时允许，完成后转为「空闲净房」
     * - 维修工单（TYPE_REPAIR）：仅在房间为「维修中」时允许，完成后转为「空闲脏房」
     *   维修过程会弄脏房间，必须再走清扫工单才能投入使用，避免脏房被维修绕过清扫直接变净。
     * 任意类型与房态不匹配时拒绝，避免清扫工单被用来"修好"维修中的房间。
     */
    @Transactional
    public void finish(Long serviceId, Long operatorId, Long updateBy) {
        RoomService req = serviceMapper.findById(serviceId);
        if (req == null) throw new BusinessException("工单不存在");
        if (req.getStatus() != null && req.getStatus() == RoomService.STATUS_FINISHED) {
            throw new BusinessException("工单已完成");
        }
        Room room = roomMapper.findById(req.getRoomId());
        if (room == null) throw new BusinessException("房间不存在");

        int nextStatus;
        if (req.getServiceType() != null && req.getServiceType() == RoomService.TYPE_CLEAN) {
            if (room.getStatus() == null || room.getStatus() != Room.STATUS_FREE_DIRTY) {
                throw new BusinessException("当前房态不是「空闲脏房」，清扫工单无法完成");
            }
            nextStatus = Room.STATUS_FREE_CLEAN;
        } else if (req.getServiceType() != null && req.getServiceType() == RoomService.TYPE_REPAIR) {
            if (room.getStatus() == null || room.getStatus() != Room.STATUS_REPAIRING) {
                throw new BusinessException("当前房态不是「维修中」，维修工单无法完成");
            }
            nextStatus = Room.STATUS_FREE_DIRTY;
        } else {
            throw new BusinessException("工单类型未知，无法完成");
        }

        serviceMapper.updateStatus(serviceId, RoomService.STATUS_FINISHED, operatorId, updateBy);
        roomService.updateStatus(req.getRoomId(), nextStatus, updateBy);
    }
}
