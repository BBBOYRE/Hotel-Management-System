package com.oracle.test.controller;

import com.oracle.test.common.BusinessException;
import com.oracle.test.common.LoginUser;
import com.oracle.test.common.Result;
import com.oracle.test.entity.SysDict;
import com.oracle.test.service.SysDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sys/dicts")
public class SysDictController {

    @Autowired private SysDictService service;

    @GetMapping
    public Result<List<SysDict>> list() {
        return Result.ok(service.listAll());
    }

    @GetMapping("/grouped")
    public Result<Map<String, List<SysDict>>> grouped() {
        return Result.ok(service.grouped());
    }

    @GetMapping("/by-type")
    public Result<List<SysDict>> byType(@RequestParam String typeCode) {
        return Result.ok(service.listByType(typeCode));
    }

    @PostMapping
    public Result<Long> create(@RequestBody SysDict dict, LoginUser current) {
        ensureAdmin(current);
        return Result.ok(service.create(dict, current.getUserId()));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysDict dict, LoginUser current) {
        ensureAdmin(current);
        dict.setDictId(id);
        service.update(dict, current.getUserId());
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
