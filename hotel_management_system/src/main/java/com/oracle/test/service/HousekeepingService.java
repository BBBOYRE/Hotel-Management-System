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
        req.setUpdateBy(updateBy);
        if (req.getStatus() == null) req.setStatus(RoomService.STATUS_PENDING);
        serviceMapper.insert(req);
        if (req.getServiceType() != null && req.getServiceType() == RoomService.TYPE_REPAIR) {
            roomService.updateStatus(room.getRoomId(), Room.STATUS_REPAIRING, updateBy);
        }
        return req.getServiceId();
    }

    @Transactional
    public void take(Long serviceId, Long operatorId, Long updateBy) {
        serviceMapper.updateStatus(serviceId, RoomService.STATUS_DOING, operatorId, updateBy);
    }

    /** 完成工单：清扫则脏房转净房、维修则维修中转净房 */
    @Transactional
    public void finish(Long serviceId, Long operatorId, Long updateBy) {
        RoomService req = serviceMapper.findById(serviceId);
        if (req == null) throw new BusinessException("工单不存在");
        serviceMapper.updateStatus(serviceId, RoomService.STATUS_FINISHED, operatorId, updateBy);
        Room room = roomMapper.findById(req.getRoomId());
        if (room != null && (room.getStatus() == Room.STATUS_FREE_DIRTY || room.getStatus() == Room.STATUS_REPAIRING)) {
            roomService.updateStatus(req.getRoomId(), Room.STATUS_FREE_CLEAN, updateBy);
        }
    }
}
