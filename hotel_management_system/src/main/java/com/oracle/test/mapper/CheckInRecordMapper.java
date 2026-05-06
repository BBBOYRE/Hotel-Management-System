package com.oracle.test.mapper;

import com.oracle.test.entity.CheckInRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface CheckInRecordMapper {

    CheckInRecord findById(Long recordId);

    List<CheckInRecord> listByOrder(@Param("orderId") Long orderId);

    List<CheckInRecord> search(@Param("keyword") String keyword,
                               @Param("status") Integer status,
                               @Param("startDate") Date startDate,
                               @Param("endDate") Date endDate,
                               @Param("offset") int offset,
                               @Param("limit") int limit);

    long count(@Param("keyword") String keyword,
               @Param("status") Integer status,
               @Param("startDate") Date startDate,
               @Param("endDate") Date endDate);

    int insert(CheckInRecord record);

    int updateCheckOutWithVersion(@Param("recordId") Long recordId,
                                  @Param("version") Long version,
                                  @Param("updateBy") Long updateBy);

    /** 当前在住总数 */
    long countInHouse();
}
