package com.oracle.test.entity;

import com.oracle.test.common.BusinessException;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Reservation {

    public static final int STATUS_BOOKED    = 1;
    public static final int STATUS_CHECKED   = 2;
    public static final int STATUS_CANCELLED = 3;
    public static final int STATUS_NO_SHOW   = 4;

    private Long resId;
    private Long orderId;
    private Long typeId;
    private Date expectIn;
    private Date expectOut;
    private Integer resStatus;
    private Long version;
    private Date createTime;
    private Date updateTime;
    private Long updateBy;

    private String typeName;
    private String statusName;

    public void validateDates() {
        if (expectIn == null || expectOut == null) {
            throw new BusinessException("预计到店/离店时间不能为空");
        }
        if (!expectOut.after(expectIn)) {
            throw new BusinessException("离店时间必须晚于到店时间");
        }
    }

    public int nights() {
        if (expectIn == null || expectOut == null) return 0;
        long diff = expectOut.getTime() - expectIn.getTime();
        long n = TimeUnit.MILLISECONDS.toDays(diff);
        return Math.max(1, (int) n);
    }

    public boolean isPending()    { return resStatus != null && resStatus == STATUS_BOOKED; }
    public boolean isCancelled()  { return resStatus != null && resStatus == STATUS_CANCELLED; }

    public Long getResId() { return resId; }
    public void setResId(Long resId) { this.resId = resId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getTypeId() { return typeId; }
    public void setTypeId(Long typeId) { this.typeId = typeId; }
    public Date getExpectIn() { return expectIn; }
    public void setExpectIn(Date expectIn) { this.expectIn = expectIn; }
    public Date getExpectOut() { return expectOut; }
    public void setExpectOut(Date expectOut) { this.expectOut = expectOut; }
    public Integer getResStatus() { return resStatus; }
    public void setResStatus(Integer resStatus) { this.resStatus = resStatus; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
    public Long getUpdateBy() { return updateBy; }
    public void setUpdateBy(Long updateBy) { this.updateBy = updateBy; }
    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }
    public String getStatusName() { return statusName; }
    public void setStatusName(String statusName) { this.statusName = statusName; }
}
