package com.example.aiend.controller;

import com.example.aiend.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据库连接测试控制器
 * 用于测试数据库连接是否正常
 *
 * @author AI-End
 * @since 2025-12-15
 */
@RestController
@RequestMapping("/database")
@Slf4j
@RequiredArgsConstructor
public class DatabaseTestController {

    private final DataSource dataSource;

    /**
     * 测试数据库连接
     *
     * @return 数据库连接信息
     */
    @GetMapping("/test")
    public Result<Map<String, Object>> testConnection() {
        log.info("开始测试数据库连接");
        Map<String, Object> info = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            info.put("connected", true);
            info.put("database", connection.getCatalog());
            info.put("url", connection.getMetaData().getURL());
            info.put("username", connection.getMetaData().getUserName());
            info.put("driverName", connection.getMetaData().getDriverName());
            info.put("driverVersion", connection.getMetaData().getDriverVersion());
            
            log.info("数据库连接成功：{}", info);
            return Result.success(info);
        } catch (SQLException e) {
            log.error("数据库连接失败：{}", e.getMessage(), e);
            info.put("connected", false);
            info.put("error", e.getMessage());
            return Result.error(500, "数据库连接失败：" + e.getMessage());
        }
    }
}
