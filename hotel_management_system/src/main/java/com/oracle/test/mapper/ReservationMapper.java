package com.oracle.test.mapper;

import com.oracle.test.entity.Reservation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface ReservationMapper {

    Reservation findById(Long resId);

    List<Reservation> listByOrder(@Param("orderId") Long orderId);

    List<Reservation> search(@Param("keyword") String keyword,
                             @Param("status") Integer status,
                             @Param("startDate") Date startDate,
                             @Param("endDate") Date endDate,
                             @Param("offset") int offset,
                             @Param("limit") int limit);

    long count(@Param("keyword") String keyword,
               @Param("status") Integer status,
               @Param("startDate") Date startDate,
               @Param("endDate") Date endDate);

    int insert(Reservation reservation);

    int updateStatusWithVersion(@Param("resId") Long resId,
                                @Param("status") Integer status,
                                @Param("version") Long version,
                                @Param("updateBy") Long updateBy);
}
