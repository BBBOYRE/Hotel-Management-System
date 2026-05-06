package com.oracle.test.mapper;

import com.oracle.test.entity.Room;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoomMapper {

    Room findById(Long roomId);

    List<Room> search(@Param("typeId") Long typeId,
                      @Param("status") Integer status,
                      @Param("floorNum") Integer floorNum,
                      @Param("keyword") String keyword,
                      @Param("orderBy") String orderBy,
                      @Param("offset") int offset,
                      @Param("limit") int limit);

    long count(@Param("typeId") Long typeId,
               @Param("status") Integer status,
               @Param("floorNum") Integer floorNum,
               @Param("keyword") String keyword);

    List<Room> listAll();

    List<Room> listAvailableByType(@Param("typeId") Long typeId);

    int insert(Room room);

    int update(Room room);

    int updateStatusWithVersion(@Param("roomId") Long roomId,
                                @Param("status") Integer status,
                                @Param("version") Long version,
                                @Param("updateBy") Long updateBy);

    int logicDelete(@Param("roomId") Long roomId,
                    @Param("updateBy") Long updateBy);
}
