package com.oracle.test.service;

import com.oracle.test.common.BusinessException;
import com.oracle.test.common.PageResult;
import com.oracle.test.entity.HotelOrder;
import com.oracle.test.mapper.HotelOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class HotelOrderService {

    @Autowired
    private HotelOrderMapper mapper;

    public PageResult<HotelOrder> page(String keyword, Integer status,
                                       Date startDate, Date endDate, String orderBy,
                                       int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<HotelOrder> records = mapper.search(keyword, status, startDate, endDate, orderBy, offset, pageSize);
        long total = mapper.count(keyword, status, startDate, endDate);
        return new PageResult<>(total, records, pageNum, pageSize);
    }

    public List<HotelOrder> list(String keyword, Integer status, Date startDate, Date endDate, String orderBy) {
        return mapper.search(keyword, status, startDate, endDate, orderBy, 0, 1000);
    }

    public HotelOrder get(Long orderId) {
        return mapper.findById(orderId);
    }

    @Transactional
    public void cancel(Long orderId, Long updateBy) {
        HotelOrder order = mapper.findById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        order.cancelOrder();
        int rows = mapper.updateStatusWithVersion(orderId, order.getStatus(), order.getVersion(), updateBy);
        if (rows == 0) throw new BusinessException("订单已被他人修改，请刷新后重试");
    }

    public List<Map<String, Object>> dailyRevenue(Date startDate, Date endDate) {
        return mapper.dailyRevenue(startDate, endDate);
    }

    public List<Map<String, Object>> roomTypeRevenue(Date startDate, Date endDate) {
        return mapper.roomTypeRevenue(startDate, endDate);
    }
}
