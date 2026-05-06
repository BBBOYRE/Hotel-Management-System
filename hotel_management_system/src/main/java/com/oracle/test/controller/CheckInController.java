package com.oracle.test.controller;

import com.oracle.test.common.LoginUser;
import com.oracle.test.common.PageResult;
import com.oracle.test.common.Result;
import com.oracle.test.entity.CheckInRecord;
import com.oracle.test.entity.GuestRecord;
import com.oracle.test.mapper.GuestRecordMapper;
import com.oracle.test.service.CheckInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/check-ins")
public class CheckInController {

    @Autowired private CheckInService service;
    @Autowired private GuestRecordMapper guestMapper;

    @GetMapping
    public Result<PageResult<CheckInRecord>> page(@RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) Integer status,
                                                  @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                                  @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
                                                  @RequestParam(defaultValue = "1") int pageNum,
                                                  @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(service.page(keyword, status, startDate, endDate, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<CheckInRecord> get(@PathVariable Long id) {
        return Result.ok(service.get(id));
    }

    @GetMapping("/{id}/guests")
    public Result<List<GuestRecord>> guests(@PathVariable Long id) {
        return Result.ok(guestMapper.listByRecord(id));
    }

    @GetMapping("/by-order/{orderId}")
    public Result<List<CheckInRecord>> byOrder(@PathVariable Long orderId) {
        return Result.ok(service.listByOrder(orderId));
    }

    @PostMapping
    public Result<CheckInRecord> checkIn(@RequestBody Map<String, Object> body, LoginUser current) {
        Long resId = toLong(body.get("resId"));
        Long roomId = toLong(body.get("roomId"));
        Long customerId = toLong(body.get("customerId"));
        @SuppressWarnings("unchecked")
        List<Map<String, String>> guests = (List<Map<String, String>>) body.get("guests");
        java.util.List<GuestRecord> guestList = new java.util.ArrayList<>();
        if (guests != null) {
            for (Map<String, String> g : guests) {
                GuestRecord gr = new GuestRecord();
                gr.setGuestName(g.get("guestName"));
                gr.setIdCard(g.get("idCard"));
                guestList.add(gr);
            }
        }
        return Result.ok(service.checkIn(resId, roomId, customerId, guestList, current.getUserId()));
    }

    @PostMapping("/{id}/check-out")
    public Result<Void> checkOut(@PathVariable Long id, LoginUser current) {
        service.checkOut(id, current.getUserId());
        return Result.ok();
    }

    @PostMapping("/{id}/change-room")
    public Result<Void> changeRoom(@PathVariable Long id,
                                   @RequestBody Map<String, Object> body,
                                   LoginUser current) {
        service.changeRoom(id, toLong(body.get("roomId")), current.getUserId());
        return Result.ok();
    }

    private static Long toLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).longValue();
        String s = o.toString();
        if (s.isEmpty() || "null".equalsIgnoreCase(s)) return null;
        return Long.valueOf(s);
    }
}
