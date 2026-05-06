package com.oracle.test.mapper;

import com.oracle.test.entity.RoomService;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoomServiceMapper {

    RoomService findById(Long serviceId);

    List<RoomService> search(@Param("status") Integer status,
                             @Param("serviceType") Integer serviceType,
                             @Param("offset") int offset,
                             @Param("limit") int limit);

    long count(@Param("status") Integer status,
               @Param("serviceType") Integer serviceType);

    int insert(RoomService service);

    int updateStatus(@Param("serviceId") Long serviceId,
                     @Param("status") Integer status,
                     @Param("operatorId") Long operatorId,
                     @Param("updateBy") Long updateBy);
}
