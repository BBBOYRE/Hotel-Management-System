package com.oracle.test.controller;

import com.oracle.test.common.BusinessException;
import com.oracle.test.common.LoginUser;
import com.oracle.test.common.PageResult;
import com.oracle.test.common.Result;
import com.oracle.test.entity.SysRole;
import com.oracle.test.entity.SysUser;
import com.oracle.test.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sys/users")
public class SysUserController {

    @Autowired private SysUserService service;

    @GetMapping
    public Result<PageResult<SysUser>> page(@RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) Integer status,
                                            @RequestParam(defaultValue = "1") int pageNum,
                                            @RequestParam(defaultValue = "10") int pageSize,
                                            LoginUser current) {
        ensureAdmin(current);
        return Result.ok(service.page(keyword, status, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<SysUser> get(@PathVariable Long id) {
        return Result.ok(service.get(id));
    }

    @GetMapping("/roles")
    public Result<List<SysRole>> roles() {
        return Result.ok(service.roles());
    }

    @PostMapping
    public Result<Long> create(@RequestBody Map<String, Object> body, LoginUser current) {
        ensureAdmin(current);
        SysUser u = new SysUser();
        u.setUsername((String) body.get("username"));
        u.setRealName((String) body.get("realName"));
        u.setRoleId(toLong(body.get("roleId")));
        u.setStatus(toInt(body.get("status")));
        return Result.ok(service.create(u, (String) body.get("password"), current.getUserId()));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody SysUser user,
                               LoginUser current) {
        ensureAdmin(current);
        user.setUserId(id);
        service.update(user, current.getUserId());
        return Result.ok();
    }

    @PostMapping("/{id}/status")
    public Result<Void> switchStatus(@PathVariable Long id,
                                     @RequestBody Map<String, Integer> body,
                                     LoginUser current) {
        ensureAdmin(current);
        service.switchStatus(id, body.get("status"), current.getUserId());
        return Result.ok();
    }

    @PostMapping("/{id}/resetPassword")
    public Result<Void> resetPassword(@PathVariable Long id,
                                      @RequestBody Map<String, String> body,
                                      LoginUser current) {
        ensureAdmin(current);
        service.resetPassword(id, body.get("password"), current.getUserId());
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, LoginUser current) {
        ensureAdmin(current);
        service.delete(id, current.getUserId());
        return Result.ok();
    }

    private static void ensureAdmin(LoginUser u) {
        if (u == null || !u.isAdmin()) throw new BusinessException(403, "仅管理员可执行此操作");
    }

    private static Long toLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).longValue();
        return Long.valueOf(o.toString());
    }

    private static Integer toInt(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).intValue();
        return Integer.valueOf(o.toString());
    }
}
