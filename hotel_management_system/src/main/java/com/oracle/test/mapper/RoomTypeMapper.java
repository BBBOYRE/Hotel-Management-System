package com.oracle.test.mapper;

import com.oracle.test.entity.RoomType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoomTypeMapper {

    List<RoomType> listAll();

    RoomType findById(Long typeId);

    int insert(RoomType type);

    int update(RoomType type);

    int updateWithVersion(RoomType type);

    int logicDelete(@Param("typeId") Long typeId,
                    @Param("updateBy") Long updateBy);
}
