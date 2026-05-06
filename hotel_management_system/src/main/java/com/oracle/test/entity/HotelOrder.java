package com.oracle.test.entity;

import com.oracle.test.common.BusinessException;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class HotelOrder {

    public static final int STATUS_ONGOING   = 1;
    public static final int STATUS_SETTLED   = 2;
    public static final int STATUS_CANCELLED = 3;

    private Long orderId;
    private String orderNo;
    private Long customerId;
    private BigDecimal totalAmount;
    private Integer status;
    private Integer isDeleted;
    private Long version;
    private Date createTime;
    private Date updateTime;
    private Long updateBy;

    private String customerName;
    private String statusName;

    public BigDecimal calculateTotalAmount(List<BillItem> items) {
        BigDecimal sum = BigDecimal.ZERO;
        if (items != null) {
            for (BillItem item : items) {
                if (item.getAmount() != null && (item.getIsDeleted() == null || item.getIsDeleted() == 0)) {
                    sum = sum.add(item.getAmount());
                }
            }
        }
        this.totalAmount = sum;
        return sum;
    }

    public void cancelOrder() {
        if (status != null && status == STATUS_SETTLED) {
            throw new BusinessException("已结算订单不可取消");
        }
        this.status = STATUS_CANCELLED;
    }

    public void settle() {
        if (status != null && status == STATUS_CANCELLED) {
            throw new BusinessException("已取消订单不可结算");
        }
        this.status = STATUS_SETTLED;
    }

    public boolean isSettled()  { return status != null && status == STATUS_SETTLED; }
    public boolean isOngoing()  { return status != null && status == STATUS_ONGOING; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
    public Long getUpdateBy() { return updateBy; }
    public void setUpdateBy(Long updateBy) { this.updateBy = updateBy; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getStatusName() { return statusName; }
    public void setStatusName(String statusName) { this.statusName = statusName; }
}
