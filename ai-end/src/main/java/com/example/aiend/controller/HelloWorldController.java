package com.example.aiend.controller;

import com.example.aiend.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hello World 控制器
 * 提供简单的问候接口
 *
 * @author AI-End
 * @since 2025-12-13
 */
@RestController
@RequestMapping("/hello")
@Slf4j
public class HelloWorldController {

    /**
     * Hello World 接口
     *
     * @return 问候消息
     */
    @GetMapping
    public Result<String> hello() {
        log.info("Hello World 接口被调用");
        return Result.success("Hello World! Welcome to AI-End Spring Boot Application!");
    }

    /**
     * 个性化问候接口
     *
     * @param name 姓名
     * @return 个性化问候消息
     */
    @GetMapping("/greet")
    public Result<String> greet(String name) {
        log.info("个性化问候接口被调用，name: {}", name);
        String message = (name == null || name.trim().isEmpty()) 
            ? "Hello World!" 
            : "Hello, " + name + "!";
        return Result.success(message);
    }
}
