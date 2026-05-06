package com.oracle.test.common;

import java.io.Serializable;

public class LoginUser implements Serializable {

    private Long userId;
    private String username;
    private String realName;
    private Long roleId;
    private String roleName;

    public LoginUser() {}

    public LoginUser(Long userId, String username, String realName, Long roleId, String roleName) {
        this.userId = userId;
        this.username = username;
        this.realName = realName;
        this.roleId = roleId;
        this.roleName = roleName;
    }

    public boolean isAdmin() {
        return roleId != null && roleId == 1L;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
}
