package com.oracle.test.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

public class LoginInterceptor implements HandlerInterceptor {

    public static final String SESSION_KEY = "LOGIN_USER";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/auth/")) {
            return true;
        }
        HttpSession session = request.getSession(false);
        Object user = session == null ? null : session.getAttribute(SESSION_KEY);
        if (user == null) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getOutputStream().write("{\"code\":401,\"msg\":\"未登录或会话已过期\"}".getBytes(StandardCharsets.UTF_8));
            return false;
        }
        request.setAttribute(SESSION_KEY, user);
        return true;
    }
}
