package com.oracle.test.controller;

import com.oracle.test.common.BusinessException;
import com.oracle.test.common.LoginUser;
import com.oracle.test.common.Result;
import com.oracle.test.service.SystemClock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 虚拟时钟管理接口：仅管理员可写，所有人可读。
 */
@RestController
@RequestMapping("/api/system/clock")
public class SystemClockController {

    @Autowired private SystemClock clock;

    @GetMapping
    public Result<Map<String, Object>> get() {
        Map<String, Object> m = new HashMap<>();
        m.put("now", clock.now());
        m.put("offsetDays", clock.getOffsetDays());
        return Result.ok(m);
    }

    @PostMapping("/advance")
    public Result<Map<String, Object>> advance(@RequestBody Map<String, Object> body, LoginUser current) {
        ensureAdmin(current);
        Object d = body.get("days");
        if (d == null) throw new BusinessException("days 不能为空");
        double days = Double.parseDouble(d.toString());
        double off = clock.advance(days);
        Map<String, Object> m = new HashMap<>();
        m.put("now", clock.now());
        m.put("offsetDays", off);
        return Result.ok(m);
    }

    @PostMapping("/set")
    public Result<Map<String, Object>> set(@RequestBody Map<String, Object> body, LoginUser current) {
        ensureAdmin(current);
        Object t = body.get("target");
        if (t == null) throw new BusinessException("target 不能为空");
        Date target;
        try {
            String s = t.toString().replace('T', ' ');
            // 接受 yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd
            java.text.SimpleDateFormat fmt = s.length() <= 10
                    ? new java.text.SimpleDateFormat("yyyy-MM-dd")
                    : new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            target = fmt.parse(s);
        } catch (Exception e) {
            throw new BusinessException("时间格式无效，应为 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss");
        }
        double off = clock.setVirtualTime(target);
        Map<String, Object> m = new HashMap<>();
        m.put("now", clock.now());
        m.put("offsetDays", off);
        return Result.ok(m);
    }

    @PostMapping("/reset")
    public Result<Map<String, Object>> reset(LoginUser current) {
        ensureAdmin(current);
        clock.reset();
        Map<String, Object> m = new HashMap<>();
        m.put("now", clock.now());
        m.put("offsetDays", 0d);
        return Result.ok(m);
    }

    private static void ensureAdmin(LoginUser u) {
        if (u == null || !u.isAdmin()) throw new BusinessException(403, "仅管理员可调整虚拟时钟");
    }
}
