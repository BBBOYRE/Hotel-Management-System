package com.oracle.test.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TestController {

    // 在我们引入 MyBatis 具体 Mapper 之前，先用 Spring 自带的 JdbcTemplate 测一下连接
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 测试接口: http://localhost:8080/api/version
     * 前端就可以用 Axios 发起 GET 请求访问这个地址
     */
    @GetMapping("/version")
    public Map<String, Object> getDatabaseVersion() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 执行刚才 OracleTest.java 里的那句测试 SQL
            List<String> versions = jdbcTemplate.queryForList("SELECT * FROM v$version", String.class);
            result.put("code", 200);
            result.put("msg", "连接成功");
            result.put("data", versions);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "查询失败: " + e.getMessage());
        }
        return result;
    }
}
