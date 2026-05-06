package com.oracle.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OracleApplication {
    public static void main(String[] args) {
        // 启动 Spring Boot 应用
        SpringApplication.run(OracleApplication.class, args);
        System.out.println("✅ Spring Boot 启动成功！后端接口已在 http://localhost:8080 就绪。");
    }
}
