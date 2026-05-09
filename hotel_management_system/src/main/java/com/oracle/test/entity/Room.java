package com.oracle.test.entity;

import com.oracle.test.common.BusinessException;

import java.util.Date;

public class Room {

    public static final int STATUS_FREE_CLEAN = 1;
    public static final int STATUS_OCCUPIED   = 3;
    public static final int STATUS_FREE_DIRTY = 4;
    public static final int STATUS_REPAIRING  = 5;

    private Long roomId;
    private String roomNo;
    private Long typeId;
    private Integer floorNum;
    private Integer status;
    private Integer isDeleted;
    private Long version;
    private Date createTime;
    private Date updateTime;
    private Long updateBy;

    private String typeName;
    private String statusName;
    private Boolean hasImage;

    public boolean isAvailable() {
        return status != null && status == STATUS_FREE_CLEAN;
    }

    public void assertCanCheckIn() {
        if (status == null || status != STATUS_FREE_CLEAN) {
            throw new BusinessException("房间当前状态不可入住");
        }
    }

    public void markOccupied()   { this.status = STATUS_OCCUPIED;   }
    public void markFreeDirty()  { this.status = STATUS_FREE_DIRTY; }
    public void markFreeClean()  { this.status = STATUS_FREE_CLEAN; }
    public void markRepairing()  { this.status = STATUS_REPAIRING;  }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
    public String getRoomNo() { return roomNo; }
    public void setRoomNo(String roomNo) { this.roomNo = roomNo; }
    public Long getTypeId() { return typeId; }
    public void setTypeId(Long typeId) { this.typeId = typeId; }
    public Integer getFloorNum() { return floorNum; }
    public void setFloorNum(Integer floorNum) { this.floorNum = floorNum; }
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
    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }
    public String getStatusName() { return statusName; }
    public void setStatusName(String statusName) { this.statusName = statusName; }
    public Boolean getHasImage() { return hasImage; }
    public void setHasImage(Boolean hasImage) { this.hasImage = hasImage; }
}
