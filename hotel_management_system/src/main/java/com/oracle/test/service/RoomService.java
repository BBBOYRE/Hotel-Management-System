package com.oracle.test.service;

import com.oracle.test.common.BusinessException;
import com.oracle.test.common.PageResult;
import com.oracle.test.entity.Room;
import com.oracle.test.mapper.RoomMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoomService {

    @Autowired
    private RoomMapper mapper;

    public PageResult<Room> page(Long typeId, Integer status, Integer floorNum,
                                 String keyword, String orderBy,
                                 int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<Room> records = mapper.search(typeId, status, floorNum, keyword, orderBy, offset, pageSize);
        long total = mapper.count(typeId, status, floorNum, keyword);
        return new PageResult<>(total, records, pageNum, pageSize);
    }

    public List<Room> listAll() {
        return mapper.listAll();
    }

    public Room get(Long roomId) {
        return mapper.findById(roomId);
    }

    public List<Room> listAvailable(Long typeId) {
        return mapper.listAvailableByType(typeId);
    }

    @Transactional
    public Long create(Room room, Long updateBy) {
        room.setUpdateBy(updateBy);
        mapper.insert(room);
        return room.getRoomId();
    }

    @Transactional
    public void update(Room room, Long updateBy) {
        room.setUpdateBy(updateBy);
        mapper.update(room);
    }

    /** 利用乐观锁更新房态，失败抛业务异常，调用方需感知。 */
    @Transactional
    public void updateStatus(Long roomId, Integer status, Long updateBy) {
        Room db = mapper.findById(roomId);
        if (db == null) throw new BusinessException("房间不存在");
        int rows = mapper.updateStatusWithVersion(roomId, status, db.getVersion(), updateBy);
        if (rows == 0) throw new BusinessException("房间状态被他人修改，请刷新后重试");
    }

    @Transactional
    public void delete(Long roomId, Long updateBy) {
        mapper.logicDelete(roomId, updateBy);
    }
}
