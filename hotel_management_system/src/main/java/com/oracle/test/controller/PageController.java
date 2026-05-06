package com.oracle.test.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    /** SPA 入口：所有非 /api、非静态资源请求都转发到首页，由前端路由处理。 */
    @GetMapping({"/", "/login", "/dashboard", "/customers", "/rooms", "/reservations",
                 "/check-ins", "/orders", "/bills", "/reports", "/users", "/dicts"})
    public String index() {
        return "forward:/index.html";
    }
}
