package com.oracle.test.entity;

import java.math.BigDecimal;
import java.util.Date;

public class BillItem {

    public static final int TYPE_ROOM_FEE     = 1;
    public static final int TYPE_DEPOSIT      = 2;
    public static final int TYPE_REFUND_DEPO  = 3;
    public static final int TYPE_PENALTY      = 4;
    public static final int TYPE_COMPENSATION = 5;
    public static final int TYPE_OTHER        = 6;

    private Long itemId;
    private Long orderId;
    private Long segmentId;
    private Integer itemType;
    private BigDecimal amount;
    private String remark;
    private Long operatorId;
    private Integer isDeleted;
    private Date recordTime;

    private String typeName;
    private String operatorName;

    public boolean isIncome() {
        return amount != null && amount.signum() > 0;
    }

    public boolean isRefund() {
        return amount != null && amount.signum() < 0;
    }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getSegmentId() { return segmentId; }
    public void setSegmentId(Long segmentId) { this.segmentId = segmentId; }
    public Integer getItemType() { return itemType; }
    public void setItemType(Integer itemType) { this.itemType = itemType; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }
    public Date getRecordTime() { return recordTime; }
    public void setRecordTime(Date recordTime) { this.recordTime = recordTime; }
    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
}
