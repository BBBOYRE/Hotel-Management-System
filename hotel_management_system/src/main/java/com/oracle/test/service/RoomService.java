package com.oracle.test.service;

import com.oracle.test.common.BusinessException;
import com.oracle.test.common.PageResult;
import com.oracle.test.entity.Room;
import com.oracle.test.mapper.RoomMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class RoomService {

    private static final long  MAX_IMAGE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif");

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

    /** 取房间图片；不存在或为空返回 null。返回 Map 含 image / imageType / imageName。 */
    public Map<String, Object> getImage(Long roomId) {
        Map<String, Object> m = mapper.findImage(roomId);
        if (m == null || m.get("image") == null) return null;
        return m;
    }

    /** 上传/替换图片；imageBytes/contentType 为空则视为清空。 */
    @Transactional
    public void saveImage(Long roomId, byte[] imageBytes, String contentType, String fileName, Long updateBy) {
        Room room = mapper.findById(roomId);
        if (room == null) throw new BusinessException("房间不存在");
        if (imageBytes != null && imageBytes.length > 0) {
            if (imageBytes.length > MAX_IMAGE_BYTES) {
                throw new BusinessException("图片超过 5MB 限制");
            }
            String mime = contentType == null ? "" : contentType.toLowerCase();
            if (!ALLOWED_IMAGE_TYPES.contains(mime)) {
                throw new BusinessException("仅支持 jpg / png / webp / gif");
            }
        } else {
            imageBytes = null;
            contentType = null;
            fileName = null;
        }
        mapper.updateImage(roomId, imageBytes, contentType, fileName, updateBy);
    }

    @Transactional
    public void clearImage(Long roomId, Long updateBy) {
        saveImage(roomId, null, null, null, updateBy);
    }
}

