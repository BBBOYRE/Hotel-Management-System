package com.oracle.test.entity;

import com.oracle.test.common.BusinessException;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class CheckInRecord {

    public static final int STATUS_IN  = 1;
    public static final int STATUS_OUT = 2;

    private Long recordId;
    private Long orderId;
    private Long resId;
    private Long roomId;
    private Date checkIn;
    private Date checkOut;
    private Integer status;
    private Long version;
    private Date createTime;
    private Date updateTime;
    private Long updateBy;

    private String roomNo;
    private String typeName;
    private String statusName;
    private String orderNo;
    private String customerName;
    private Integer segNo;
    private Integer segTotal;

    public void doCheckOut() {
        if (status != null && status == STATUS_OUT) {
            throw new BusinessException("该入住记录已退房");
        }
        this.status = STATUS_OUT;
        this.checkOut = new Date();
    }

    public int nights() {
        if (checkIn == null) return 0;
        Date end = checkOut == null ? new Date() : checkOut;
        long diff = end.getTime() - checkIn.getTime();
        long n = TimeUnit.MILLISECONDS.toDays(diff);
        return Math.max(1, (int) n);
    }

    public boolean isInHouse() { return status != null && status == STATUS_IN; }

    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getResId() { return resId; }
    public void setResId(Long resId) { this.resId = resId; }
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
    public Date getCheckIn() { return checkIn; }
    public void setCheckIn(Date checkIn) { this.checkIn = checkIn; }
    public Date getCheckOut() { return checkOut; }
    public void setCheckOut(Date checkOut) { this.checkOut = checkOut; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
    public Long getUpdateBy() { return updateBy; }
    public void setUpdateBy(Long updateBy) { this.updateBy = updateBy; }
    public String getRoomNo() { return roomNo; }
    public void setRoomNo(String roomNo) { this.roomNo = roomNo; }
    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }
    public String getStatusName() { return statusName; }
    public void setStatusName(String statusName) { this.statusName = statusName; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public Integer getSegNo() { return segNo; }
    public void setSegNo(Integer segNo) { this.segNo = segNo; }
    public Integer getSegTotal() { return segTotal; }
    public void setSegTotal(Integer segTotal) { this.segTotal = segTotal; }
}
