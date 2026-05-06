package com.oracle.test.entity;

import java.math.BigDecimal;
import java.util.Date;

public class RoomType {

    private Long typeId;
    private String typeName;
    private BigDecimal basePrice;
    private Integer bedCount;
    private Integer isDeleted;
    private Long version;
    private Date createTime;
    private Date updateTime;
    private Long updateBy;

    public BigDecimal calcAmount(int nights) {
        if (basePrice == null) return BigDecimal.ZERO;
        return basePrice.multiply(BigDecimal.valueOf(nights));
    }

    public Long getTypeId() { return typeId; }
    public void setTypeId(Long typeId) { this.typeId = typeId; }
    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }
    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
    public Integer getBedCount() { return bedCount; }
    public void setBedCount(Integer bedCount) { this.bedCount = bedCount; }
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
}
