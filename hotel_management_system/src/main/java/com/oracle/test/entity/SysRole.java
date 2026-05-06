package com.oracle.test.entity;

import java.util.Date;

public class SysRole {

    public static final long ROLE_ADMIN = 1L;
    public static final long ROLE_RECEPTION = 2L;
    public static final long ROLE_FINANCE = 3L;

    private Long roleId;
    private String roleName;
    private String description;
    private Integer isDeleted;
    private Date createTime;
    private Date updateTime;
    private Long updateBy;

    public boolean isAdmin() {
        return roleId != null && roleId == ROLE_ADMIN;
    }

    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
    public Long getUpdateBy() { return updateBy; }
    public void setUpdateBy(Long updateBy) { this.updateBy = updateBy; }
}
