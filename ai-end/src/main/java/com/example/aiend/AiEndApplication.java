package com.example.aiend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot 应用主启动类
 *
 * @author AI-End
 * @since 2025-12-13
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class AiEndApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiEndApplication.class, args);
    }

}
