package com.oracle.test.controller;

import com.oracle.test.common.LoginUser;
import com.oracle.test.common.PageResult;
import com.oracle.test.common.Result;
import com.oracle.test.entity.Customer;
import com.oracle.test.service.CustomerService;
import com.oracle.test.util.ExcelExportUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired private CustomerService service;

    @GetMapping
    public Result<PageResult<Customer>> page(@RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) Integer vipLevel,
                                             @RequestParam(required = false) String orderBy,
                                             @RequestParam(defaultValue = "1") int pageNum,
                                             @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(service.page(keyword, vipLevel, orderBy, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<Customer> get(@PathVariable Long id) {
        return Result.ok(service.get(id));
    }

    @GetMapping("/by-id-card")
    public Result<Customer> byIdCard(@RequestParam String idCard) {
        return Result.ok(service.findByIdCard(idCard));
    }

    @PostMapping
    public Result<Long> create(@RequestBody Customer customer, LoginUser current) {
        current.require("customer:edit");
        return Result.ok(service.create(customer, current.getUserId()));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Customer customer, LoginUser current) {
        current.require("customer:edit");
        customer.setCustomerId(id);
        service.update(customer, current.getUserId());
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, LoginUser current) {
        current.require("customer:delete");
        service.delete(id, current.getUserId());
        return Result.ok();
    }

    @GetMapping("/export")
    public void export(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Integer vipLevel,
                       HttpServletResponse response) throws IOException {
        PageResult<Customer> all = service.page(keyword, vipLevel, null, 1, 10000);
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("customerId", "客户ID");
        headers.put("custName",   "姓名");
        headers.put("idCard",     "身份证号");
        headers.put("phone",      "手机号");
        headers.put("genderText", "性别");
        headers.put("vipLevel",   "会员等级");
        headers.put("createTime", "建档时间");

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Customer c : all.getRecords()) {
            Map<String, Object> row = new HashMap<>();
            row.put("customerId", c.getCustomerId());
            row.put("custName",   c.getCustName());
            row.put("idCard",     c.getIdCard());
            row.put("phone",      c.getPhone());
            row.put("genderText", c.getGender() == null ? "" : (c.getGender() == 1 ? "男" : "女"));
            row.put("vipLevel",   c.getVipLevel());
            row.put("createTime", c.getCreateTime());
            rows.add(row);
        }
        ExcelExportUtil.export(response, "客户档案_" + System.currentTimeMillis(), headers, rows);
    }
}
