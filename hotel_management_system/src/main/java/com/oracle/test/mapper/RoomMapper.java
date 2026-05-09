package com.oracle.test.mapper;

import com.oracle.test.entity.Room;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

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

    /** 返回 image / imageType / imageName 三列；image 为空表示无图 */
    Map<String, Object> findImage(@Param("roomId") Long roomId);

    int updateImage(@Param("roomId") Long roomId,
                    @Param("image") byte[] image,
                    @Param("imageType") String imageType,
                    @Param("imageName") String imageName,
                    @Param("updateBy") Long updateBy);
}
