package com.oracle.test.entity;

import com.oracle.test.common.BusinessException;
import com.oracle.test.util.PasswordUtil;

import java.util.Date;

public class SysUser {

    private Long userId;
    private String username;
    private String password;
    private String realName;
    private Long roleId;
    private Integer status;
    private Integer isDeleted;
    private Date createTime;
    private Date updateTime;
    private Long updateBy;

    private String roleName;

    public void encryptPassword(String rawPassword) {
        if (!PasswordUtil.isStrong(rawPassword)) {
            throw new BusinessException("密码强度不足：至少 6 位且包含字母与数字");
        }
        this.password = PasswordUtil.encrypt(rawPassword);
    }

    public boolean verifyPassword(String rawPassword) {
        return PasswordUtil.matches(rawPassword, this.password);
    }

    public boolean isEnabled() {
        return status != null && status == 1;
    }

    public boolean isAdmin() {
        return roleId != null && roleId == SysRole.ROLE_ADMIN;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
    public Long getUpdateBy() { return updateBy; }
    public void setUpdateBy(Long updateBy) { this.updateBy = updateBy; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
}
