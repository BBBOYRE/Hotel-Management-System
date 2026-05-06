package com.oracle.test.controller;

import com.oracle.test.common.LoginUser;
import com.oracle.test.common.PageResult;
import com.oracle.test.common.Result;
import com.oracle.test.entity.BillItem;
import com.oracle.test.service.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bills")
public class BillController {

    @Autowired private BillService service;

    @GetMapping
    public Result<PageResult<BillItem>> page(@RequestParam(required = false) Long orderId,
                                             @RequestParam(required = false) Integer itemType,
                                             @RequestParam(required = false) Long operatorId,
                                             @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                             @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
                                             @RequestParam(defaultValue = "1") int pageNum,
                                             @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(service.page(orderId, itemType, startDate, endDate, operatorId, pageNum, pageSize));
    }

    @GetMapping("/by-order/{orderId}")
    public Result<List<BillItem>> byOrder(@PathVariable Long orderId) {
        return Result.ok(service.listByOrder(orderId));
    }

    @GetMapping("/sum/{orderId}")
    public Result<BigDecimal> sum(@PathVariable Long orderId) {
        return Result.ok(service.sumByOrder(orderId));
    }

    @PostMapping
    public Result<Long> add(@RequestBody BillItem item, LoginUser current) {
        return Result.ok(service.addItem(item, current.getUserId()));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, LoginUser current) {
        service.removeItem(id, current.getUserId());
        return Result.ok();
    }

    @PostMapping("/settle")
    public Result<BigDecimal> settle(@RequestBody Map<String, Object> body, LoginUser current) {
        Long orderId = ((Number) body.get("orderId")).longValue();
        return Result.ok(service.settle(orderId, current.getUserId()));
    }

    @GetMapping("/shift")
    public Result<Map<String, Object>> shift(@RequestParam(required = false) Long operatorId,
                                             @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startDate,
                                             @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate,
                                             LoginUser current) {
        Long opId = operatorId == null ? current.getUserId() : operatorId;
        return Result.ok(service.shiftSummary(opId, startDate, endDate));
    }
}
