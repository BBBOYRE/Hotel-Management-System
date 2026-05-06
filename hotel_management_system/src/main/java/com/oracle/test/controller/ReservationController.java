package com.oracle.test.controller;

import com.oracle.test.common.LoginUser;
import com.oracle.test.common.PageResult;
import com.oracle.test.common.Result;
import com.oracle.test.entity.Reservation;
import com.oracle.test.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    @Autowired private ReservationService service;

    @GetMapping
    public Result<PageResult<Reservation>> page(@RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) Integer status,
                                                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
                                                @RequestParam(defaultValue = "1") int pageNum,
                                                @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(service.page(keyword, status, startDate, endDate, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<Reservation> get(@PathVariable Long id) {
        return Result.ok(service.get(id));
    }

    @GetMapping("/by-order/{orderId}")
    public Result<List<Reservation>> byOrder(@PathVariable Long orderId) {
        return Result.ok(service.listByOrder(orderId));
    }

    @PostMapping
    public Result<Reservation> book(@RequestBody Map<String, Object> body, LoginUser current) {
        Long customerId = toLong(body.get("customerId"));
        Reservation form = new Reservation();
        form.setTypeId(toLong(body.get("typeId")));
        form.setExpectIn(toDate(body.get("expectIn")));
        form.setExpectOut(toDate(body.get("expectOut")));
        return Result.ok(service.book(customerId, form, current.getUserId()));
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id, LoginUser current) {
        service.cancel(id, current.getUserId());
        return Result.ok();
    }

    @PostMapping("/{id}/no-show")
    public Result<Void> noShow(@PathVariable Long id, LoginUser current) {
        service.markNoShow(id, current.getUserId());
        return Result.ok();
    }

    private static Long toLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).longValue();
        return Long.valueOf(o.toString());
    }

    private static Date toDate(Object o) {
        if (o == null) return null;
        try {
            String s = o.toString();
            if (s.length() == 10) {
                return new java.text.SimpleDateFormat("yyyy-MM-dd").parse(s);
            }
            return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(s.replace('T', ' '));
        } catch (Exception e) {
            throw new IllegalArgumentException("日期格式错误: " + o);
        }
    }
}
