package com.oracle.test.service;

import com.oracle.test.common.BusinessException;
import com.oracle.test.common.PageResult;
import com.oracle.test.entity.BillItem;
import com.oracle.test.entity.HotelOrder;
import com.oracle.test.mapper.BillItemMapper;
import com.oracle.test.mapper.HotelOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class BillService {

    @Autowired private BillItemMapper itemMapper;
    @Autowired private HotelOrderMapper orderMapper;

    public PageResult<BillItem> page(Long orderId, Integer itemType, Date startDate,
                                     Date endDate, Long operatorId, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<BillItem> records = itemMapper.search(orderId, itemType, startDate, endDate, operatorId, offset, pageSize);
        long total = itemMapper.count(orderId, itemType, startDate, endDate, operatorId);
        return new PageResult<>(total, records, pageNum, pageSize);
    }

    public List<BillItem> listByOrder(Long orderId) {
        return itemMapper.listByOrder(orderId);
    }

    public BigDecimal sumByOrder(Long orderId) {
        return itemMapper.sumByOrder(orderId);
    }

    @Transactional
    public Long addItem(BillItem item, Long operatorId) {
        if (item.getOrderId() == null) throw new BusinessException("缺少订单 ID");
        if (item.getAmount() == null) throw new BusinessException("金额不能为空");
        item.setOperatorId(operatorId);
        itemMapper.insert(item);
        BigDecimal sum = itemMapper.sumByOrder(item.getOrderId());
        orderMapper.updateAmount(item.getOrderId(), sum, operatorId);
        return item.getItemId();
    }

    @Transactional
    public void removeItem(Long itemId, Long operatorId) {
        BillItem item = itemMapper.findById(itemId);
        if (item == null) throw new BusinessException("账单不存在");
        itemMapper.logicDelete(itemId);
        BigDecimal sum = itemMapper.sumByOrder(item.getOrderId());
        orderMapper.updateAmount(item.getOrderId(), sum, operatorId);
    }

    /** 订单结算：计算总金额并标记订单已结算（乐观锁）。 */
    @Transactional
    public BigDecimal settle(Long orderId, Long operatorId) {
        HotelOrder order = orderMapper.findById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        if (order.isSettled()) throw new BusinessException("订单已结算");

        BigDecimal sum = itemMapper.sumByOrder(orderId);
        orderMapper.updateAmount(orderId, sum, operatorId);

        int rows = orderMapper.updateStatusWithVersion(
                orderId, HotelOrder.STATUS_SETTLED, order.getVersion(), operatorId);
        if (rows == 0) throw new BusinessException("订单已被他人修改，请刷新后重试");
        return sum;
    }

    /** 收银员交接班核算 */
    public Map<String, Object> shiftSummary(Long operatorId, Date startDate, Date endDate) {
        return itemMapper.shiftSummary(operatorId, startDate, endDate);
    }
}
