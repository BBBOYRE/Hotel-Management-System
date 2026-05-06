package com.oracle.test.service;

import com.oracle.test.common.BusinessException;
import com.oracle.test.common.PageResult;
import com.oracle.test.entity.HotelOrder;
import com.oracle.test.entity.Reservation;
import com.oracle.test.mapper.HotelOrderMapper;
import com.oracle.test.mapper.ReservationMapper;
import com.oracle.test.util.OrderNoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class ReservationService {

    @Autowired private ReservationMapper mapper;
    @Autowired private HotelOrderMapper orderMapper;

    public PageResult<Reservation> page(String keyword, Integer status,
                                        Date startDate, Date endDate,
                                        int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<Reservation> records = mapper.search(keyword, status, startDate, endDate, offset, pageSize);
        long total = mapper.count(keyword, status, startDate, endDate);
        return new PageResult<>(total, records, pageNum, pageSize);
    }

    public Reservation get(Long resId) {
        return mapper.findById(resId);
    }

    public List<Reservation> listByOrder(Long orderId) {
        return mapper.listByOrder(orderId);
    }

    /** 预订下单：自动创建总订单 + 预订明细 */
    @Transactional
    public Reservation book(Long customerId, Reservation form, Long updateBy) {
        form.validateDates();
        if (form.getTypeId() == null) throw new BusinessException("请选择预订房型");

        HotelOrder order = new HotelOrder();
        order.setOrderNo(OrderNoUtil.generate("HOTEL"));
        order.setCustomerId(customerId);
        order.setStatus(HotelOrder.STATUS_ONGOING);
        order.setUpdateBy(updateBy);
        orderMapper.insert(order);

        form.setOrderId(order.getOrderId());
        form.setResStatus(Reservation.STATUS_BOOKED);
        form.setUpdateBy(updateBy);
        mapper.insert(form);

        return form;
    }

    @Transactional
    public void cancel(Long resId, Long updateBy) {
        Reservation res = mapper.findById(resId);
        if (res == null) throw new BusinessException("预订不存在");
        if (!res.isPending()) throw new BusinessException("当前状态不可取消");
        int rows = mapper.updateStatusWithVersion(resId, Reservation.STATUS_CANCELLED, res.getVersion(), updateBy);
        if (rows == 0) throw new BusinessException("预订已被他人修改，请刷新后重试");
    }

    @Transactional
    public void markNoShow(Long resId, Long updateBy) {
        Reservation res = mapper.findById(resId);
        if (res == null) throw new BusinessException("预订不存在");
        int rows = mapper.updateStatusWithVersion(resId, Reservation.STATUS_NO_SHOW, res.getVersion(), updateBy);
        if (rows == 0) throw new BusinessException("预订已被他人修改，请刷新后重试");
    }
}
