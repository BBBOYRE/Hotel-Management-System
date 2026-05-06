package com.oracle.test.service;

import com.oracle.test.common.BusinessException;
import com.oracle.test.entity.RoomType;
import com.oracle.test.mapper.RoomTypeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoomTypeService {

    @Autowired
    private RoomTypeMapper mapper;

    public List<RoomType> listAll() {
        return mapper.listAll();
    }

    public RoomType get(Long typeId) {
        return mapper.findById(typeId);
    }

    @Transactional
    public Long create(RoomType type, Long updateBy) {
        if (type.getBasePrice() == null || type.getBasePrice().signum() < 0) {
            throw new BusinessException("挂牌价不能为空且需大于等于 0");
        }
        type.setUpdateBy(updateBy);
        mapper.insert(type);
        return type.getTypeId();
    }

    @Transactional
    public void update(RoomType type, Long updateBy) {
        type.setUpdateBy(updateBy);
        int rows = mapper.updateWithVersion(type);
        if (rows == 0) {
            throw new BusinessException("数据已被他人修改，请刷新后重试");
        }
    }

    @Transactional
    public void delete(Long typeId, Long updateBy) {
        mapper.logicDelete(typeId, updateBy);
    }
}
