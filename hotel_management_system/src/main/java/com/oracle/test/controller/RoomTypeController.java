package com.oracle.test.controller;

import com.oracle.test.common.LoginUser;
import com.oracle.test.common.Result;
import com.oracle.test.entity.RoomType;
import com.oracle.test.service.RoomTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/room-types")
public class RoomTypeController {

    @Autowired private RoomTypeService service;

    @GetMapping
    public Result<List<RoomType>> list() {
        return Result.ok(service.listAll());
    }

    @GetMapping("/{id}")
    public Result<RoomType> get(@PathVariable Long id) {
        return Result.ok(service.get(id));
    }

    @PostMapping
    public Result<Long> create(@RequestBody RoomType type, LoginUser current) {
        current.require("roomtype:edit");
        return Result.ok(service.create(type, current.getUserId()));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody RoomType type, LoginUser current) {
        current.require("roomtype:edit");
        type.setTypeId(id);
        service.update(type, current.getUserId());
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, LoginUser current) {
        current.require("roomtype:delete");
        service.delete(id, current.getUserId());
        return Result.ok();
    }
}
