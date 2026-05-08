package com.oracle.test.controller;

import com.oracle.test.common.LoginUser;
import com.oracle.test.common.PageResult;
import com.oracle.test.common.Result;
import com.oracle.test.entity.Room;
import com.oracle.test.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired private RoomService service;

    @GetMapping
    public Result<PageResult<Room>> page(@RequestParam(required = false) Long typeId,
                                         @RequestParam(required = false) Integer status,
                                         @RequestParam(required = false) Integer floorNum,
                                         @RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) String orderBy,
                                         @RequestParam(defaultValue = "1") int pageNum,
                                         @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(service.page(typeId, status, floorNum, keyword, orderBy, pageNum, pageSize));
    }

    @GetMapping("/all")
    public Result<List<Room>> listAll() {
        return Result.ok(service.listAll());
    }

    @GetMapping("/available")
    public Result<List<Room>> available(@RequestParam(required = false) Long typeId) {
        return Result.ok(service.listAvailable(typeId));
    }

    @GetMapping("/{id}")
    public Result<Room> get(@PathVariable Long id) {
        return Result.ok(service.get(id));
    }

    @PostMapping
    public Result<Long> create(@RequestBody Room room, LoginUser current) {
        current.require("room:edit");
        return Result.ok(service.create(room, current.getUserId()));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Room room, LoginUser current) {
        current.require("room:edit");
        room.setRoomId(id);
        service.update(room, current.getUserId());
        return Result.ok();
    }

    @PostMapping("/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id,
                                     @RequestBody Map<String, Integer> body,
                                     LoginUser current) {
        current.require("room:edit");
        service.updateStatus(id, body.get("status"), current.getUserId());
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, LoginUser current) {
        current.require("room:delete");
        service.delete(id, current.getUserId());
        return Result.ok();
    }
}
