package com.oracle.test.entity;

import com.oracle.test.common.BusinessException;

import java.util.Date;
import java.util.regex.Pattern;

public class Customer {

    private static final Pattern ID_CARD_PATTERN =
            Pattern.compile("^[1-9]\\d{5}(18|19|20)\\d{2}(0\\d|1[0-2])([0-2]\\d|3[01])\\d{3}[\\dXx]$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    private Long customerId;
    private String custName;
    private String idCard;
    private String phone;
    private Integer gender;
    private Integer vipLevel;
    private Integer isDeleted;
    private Date createTime;
    private Date updateTime;
    private Long updateBy;

    public void validate() {
        if (custName == null || custName.trim().isEmpty()) {
            throw new BusinessException("客户姓名不能为空");
        }
        if (idCard == null || !ID_CARD_PATTERN.matcher(idCard).matches()) {
            throw new BusinessException("身份证号格式不正确");
        }
        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
            throw new BusinessException("手机号格式不正确");
        }
    }

    public boolean isVip() {
        return vipLevel != null && vipLevel > 0;
    }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustName() { return custName; }
    public void setCustName(String custName) { this.custName = custName; }
    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Integer getGender() { return gender; }
    public void setGender(Integer gender) { this.gender = gender; }
    public Integer getVipLevel() { return vipLevel; }
    public void setVipLevel(Integer vipLevel) { this.vipLevel = vipLevel; }
    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
    public Long getUpdateBy() { return updateBy; }
    public void setUpdateBy(Long updateBy) { this.updateBy = updateBy; }
}
