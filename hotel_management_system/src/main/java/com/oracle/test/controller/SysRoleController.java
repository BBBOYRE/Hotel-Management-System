package com.oracle.test.controller;

import com.oracle.test.common.BusinessException;
import com.oracle.test.common.LoginUser;
import com.oracle.test.common.Result;
import com.oracle.test.entity.SysRole;
import com.oracle.test.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sys/roles")
public class SysRoleController {

    @Autowired
    private SysRoleService service;

    @GetMapping
    public Result<List<SysRole>> list() {
        return Result.ok(service.listAll());
    }

    @PostMapping
    public Result<Long> create(@RequestBody SysRole role, LoginUser current) {
        ensureAdmin(current);
        return Result.ok(service.create(role, current.getUserId()));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysRole role, LoginUser current) {
        ensureAdmin(current);
        role.setRoleId(id);
        service.update(role, current.getUserId());
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, LoginUser current) {
        ensureAdmin(current);
        service.delete(id);
        return Result.ok();
    }

    private static void ensureAdmin(LoginUser u) {
        if (u == null || !u.isAdmin()) throw new BusinessException(403, "仅管理员可执行此操作");
    }
}
