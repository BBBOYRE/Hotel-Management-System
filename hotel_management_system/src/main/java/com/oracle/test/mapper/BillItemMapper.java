package com.oracle.test.mapper;

import com.oracle.test.entity.BillItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface BillItemMapper {

    BillItem findById(Long itemId);

    List<BillItem> listByOrder(@Param("orderId") Long orderId);

    List<BillItem> search(@Param("orderId") Long orderId,
                          @Param("itemType") Integer itemType,
                          @Param("startDate") Date startDate,
                          @Param("endDate") Date endDate,
                          @Param("operatorId") Long operatorId,
                          @Param("offset") int offset,
                          @Param("limit") int limit);

    long count(@Param("orderId") Long orderId,
               @Param("itemType") Integer itemType,
               @Param("startDate") Date startDate,
               @Param("endDate") Date endDate,
               @Param("operatorId") Long operatorId);

    BigDecimal sumByOrder(@Param("orderId") Long orderId);

    int insert(BillItem item);

    int logicDelete(@Param("itemId") Long itemId);

    /** 收银员交接班核算 */
    Map<String, Object> shiftSummary(@Param("operatorId") Long operatorId,
                                     @Param("startDate") Date startDate,
                                     @Param("endDate") Date endDate);
}
