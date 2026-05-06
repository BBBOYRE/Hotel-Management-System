package com.oracle.test.entity;

import java.util.Date;

public class RoomService {

    public static final int TYPE_CLEAN  = 1;
    public static final int TYPE_REPAIR = 2;

    public static final int STATUS_PENDING  = 1;
    public static final int STATUS_DOING    = 2;
    public static final int STATUS_FINISHED = 3;

    private Long serviceId;
    private Long roomId;
    private Integer serviceType;
    private String reason;
    private Integer status;
    private Long operatorId;
    private Date finishTime;
    private Date createTime;
    private Date updateTime;
    private Long updateBy;

    private String roomNo;
    private String typeName;
    private String statusName;
    private String operatorName;

    public boolean isFinished() {
        return status != null && status == STATUS_FINISHED;
    }

    public void finish() {
        this.status = STATUS_FINISHED;
        this.finishTime = new Date();
    }

    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
    public Integer getServiceType() { return serviceType; }
    public void setServiceType(Integer serviceType) { this.serviceType = serviceType; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public Date getFinishTime() { return finishTime; }
    public void setFinishTime(Date finishTime) { this.finishTime = finishTime; }
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
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
}
