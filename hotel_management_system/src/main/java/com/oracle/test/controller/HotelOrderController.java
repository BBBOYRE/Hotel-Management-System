package com.oracle.test.controller;

import com.oracle.test.common.LoginUser;
import com.oracle.test.common.PageResult;
import com.oracle.test.common.Result;
import com.oracle.test.entity.HotelOrder;
import com.oracle.test.service.HotelOrderService;
import com.oracle.test.util.ExcelExportUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/orders")
public class HotelOrderController {

    @Autowired private HotelOrderService service;

    @GetMapping
    public Result<PageResult<HotelOrder>> page(@RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) Integer status,
                                               @RequestParam(required = false) String orderBy,
                                               @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                               @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
                                               @RequestParam(defaultValue = "1") int pageNum,
                                               @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(service.page(keyword, status, startDate, endDate, orderBy, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<HotelOrder> get(@PathVariable Long id) {
        return Result.ok(service.get(id));
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id, LoginUser current) {
        service.cancel(id, current.getUserId());
        return Result.ok();
    }

    @GetMapping("/export")
    public void export(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Integer status,
                       @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                       @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
                       HttpServletResponse response) throws IOException {
        List<HotelOrder> all = service.list(keyword, status, startDate, endDate, null);

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("orderNo",      "订单号");
        headers.put("customerName", "客户");
        headers.put("totalAmount",  "金额");
        headers.put("statusName",   "状态");
        headers.put("createTime",   "创建时间");

        List<Map<String, Object>> rows = new ArrayList<>();
        for (HotelOrder o : all) {
            Map<String, Object> r = new HashMap<>();
            r.put("orderNo",      o.getOrderNo());
            r.put("customerName", o.getCustomerName());
            r.put("totalAmount",  o.getTotalAmount());
            r.put("statusName",   o.getStatusName());
            r.put("createTime",   o.getCreateTime());
            rows.add(r);
        }
        ExcelExportUtil.export(response, "酒店订单_" + System.currentTimeMillis(), headers, rows);
    }
}
