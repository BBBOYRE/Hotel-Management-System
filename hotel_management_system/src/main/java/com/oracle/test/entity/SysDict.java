package com.oracle.test.entity;

import java.util.Date;

public class SysDict {

    public static final String TYPE_ROOM_STATUS    = "ROOM_STATUS";
    public static final String TYPE_ORDER_STATUS   = "ORDER_STATUS";
    public static final String TYPE_RES_STATUS     = "RES_STATUS";
    public static final String TYPE_CHECK_STATUS   = "CHECK_STATUS";
    public static final String TYPE_BILL_TYPE      = "BILL_TYPE";
    public static final String TYPE_SERVICE_TYPE   = "SERVICE_TYPE";
    public static final String TYPE_SERVICE_STATUS = "SERVICE_STATUS";

    private Long dictId;
    private String typeCode;
    private Integer itemValue;
    private String itemName;
    private Integer sortOrder;
    private Date createTime;
    private Date updateTime;
    private Long updateBy;

    public Long getDictId() { return dictId; }
    public void setDictId(Long dictId) { this.dictId = dictId; }
    public String getTypeCode() { return typeCode; }
    public void setTypeCode(String typeCode) { this.typeCode = typeCode; }
    public Integer getItemValue() { return itemValue; }
    public void setItemValue(Integer itemValue) { this.itemValue = itemValue; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
    public Long getUpdateBy() { return updateBy; }
    public void setUpdateBy(Long updateBy) { this.updateBy = updateBy; }
}
