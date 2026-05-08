package com.oracle.test.common;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class LoginUser implements Serializable {

    private Long userId;
    private String username;
    private String realName;
    private Long roleId;
    private String roleName;
    private Set<String> permissions = Collections.emptySet();

    public LoginUser() {}

    public LoginUser(Long userId, String username, String realName, Long roleId, String roleName, Set<String> permissions) {
        this.userId = userId;
        this.username = username;
        this.realName = realName;
        this.roleId = roleId;
        this.roleName = roleName;
        this.permissions = permissions == null ? Collections.emptySet() : permissions;
    }

    public boolean isAdmin() {
        return roleId != null && roleId == 1L;
    }

    /** 是否拥有指定权限码；管理员或权限集合中含 "*" 即视为全权 */
    public boolean has(String code) {
        if (isAdmin()) return true;
        if (permissions == null) return false;
        return permissions.contains("*") || permissions.contains(code);
    }

    /** 校验权限，缺失则抛 403 */
    public void require(String code) {
        if (!has(code)) {
            throw new BusinessException(403, "无权限：" + code);
        }
    }

    public static Set<String> parsePermissions(String csv) {
        Set<String> set = new HashSet<>();
        if (csv == null) return set;
        for (String p : csv.split(",")) {
            String t = p.trim();
            if (!t.isEmpty()) set.add(t);
        }
        return set;
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
    public Set<String> getPermissions() { return permissions; }
    public void setPermissions(Set<String> permissions) { this.permissions = permissions; }
}
