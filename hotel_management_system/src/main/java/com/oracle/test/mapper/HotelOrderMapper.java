package com.oracle.test.mapper;

import com.oracle.test.entity.HotelOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface HotelOrderMapper {

    HotelOrder findById(Long orderId);

    List<HotelOrder> search(@Param("keyword") String keyword,
                            @Param("status") Integer status,
                            @Param("startDate") Date startDate,
                            @Param("endDate") Date endDate,
                            @Param("orderBy") String orderBy,
                            @Param("offset") int offset,
                            @Param("limit") int limit);

    long count(@Param("keyword") String keyword,
               @Param("status") Integer status,
               @Param("startDate") Date startDate,
               @Param("endDate") Date endDate);

    int insert(HotelOrder order);

    int updateAmount(@Param("orderId") Long orderId,
                     @Param("totalAmount") BigDecimal totalAmount,
                     @Param("updateBy") Long updateBy);

    int updateStatusWithVersion(@Param("orderId") Long orderId,
                                @Param("status") Integer status,
                                @Param("version") Long version,
                                @Param("updateBy") Long updateBy);

    /** 按日统计营收 */
    List<Map<String, Object>> dailyRevenue(@Param("startDate") Date startDate,
                                           @Param("endDate") Date endDate);

    /** 按房型统计入住数与收益 */
    List<Map<String, Object>> roomTypeRevenue(@Param("startDate") Date startDate,
                                              @Param("endDate") Date endDate);

    /** 给定时间窗口内的总占用天数（用于计算入住率） */
    Map<String, Object> occupancyDays(@Param("startDate") Date startDate,
                                      @Param("endDate") Date endDate);
}
