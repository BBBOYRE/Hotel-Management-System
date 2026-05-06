package com.oracle.test.controller;

import com.oracle.test.common.LoginUser;
import com.oracle.test.common.PageResult;
import com.oracle.test.common.Result;
import com.oracle.test.entity.RoomService;
import com.oracle.test.service.HousekeepingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/housekeeping")
public class HousekeepingController {

    @Autowired private HousekeepingService service;

    @GetMapping
    public Result<PageResult<RoomService>> page(@RequestParam(required = false) Integer status,
                                                @RequestParam(required = false) Integer serviceType,
                                                @RequestParam(defaultValue = "1") int pageNum,
                                                @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(service.page(status, serviceType, pageNum, pageSize));
    }

    @PostMapping
    public Result<Long> create(@RequestBody RoomService req, LoginUser current) {
        return Result.ok(service.createOrder(req, current.getUserId()));
    }

    @PostMapping("/{id}/take")
    public Result<Void> take(@PathVariable Long id, LoginUser current) {
        service.take(id, current.getUserId(), current.getUserId());
        return Result.ok();
    }

    @PostMapping("/{id}/finish")
    public Result<Void> finish(@PathVariable Long id, LoginUser current) {
        service.finish(id, current.getUserId(), current.getUserId());
        return Result.ok();
    }
}
