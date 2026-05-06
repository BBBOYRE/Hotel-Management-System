package com.oracle.test.controller;

import com.oracle.test.common.LoginInterceptor;
import com.oracle.test.common.LoginUser;
import com.oracle.test.common.Result;
import com.oracle.test.service.AuthService;
import com.oracle.test.service.SysUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private AuthService authService;
    @Autowired private SysUserService userService;

    @PostMapping("/login")
    public Result<LoginUser> login(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String username = body.get("username");
        String password = body.get("password");
        LoginUser user = authService.login(username, password);
        HttpSession session = request.getSession(true);
        session.setAttribute(LoginInterceptor.SESSION_KEY, user);
        return Result.ok("登录成功", user);
    }

    @GetMapping("/me")
    public Result<LoginUser> me(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return Result.fail(401, "未登录");
        LoginUser user = (LoginUser) session.getAttribute(LoginInterceptor.SESSION_KEY);
        if (user == null) return Result.fail(401, "未登录");
        return Result.ok(user);
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return Result.ok();
    }

    @PostMapping("/changePassword")
    public Result<Void> changePassword(@RequestBody Map<String, String> body, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        LoginUser user = session == null ? null : (LoginUser) session.getAttribute(LoginInterceptor.SESSION_KEY);
        if (user == null) return Result.fail(401, "未登录");
        userService.changePassword(user.getUserId(), body.get("oldPassword"), body.get("newPassword"));
        return Result.ok();
    }
}
