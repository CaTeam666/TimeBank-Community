package com.example.aiend.controller;

import com.example.aiend.common.Result;
import com.example.aiend.dto.request.SystemSettingsDTO;
import com.example.aiend.service.SettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 系统设置控制器
 *
 * @author AI-End
 * @since 2025-12-27
 */
@RestController
@RequestMapping("/settings")
@Slf4j
@RequiredArgsConstructor
public class SettingsController {
    
    private final SettingsService settingsService;
    
    /**
     * 获取所有系统配置
     * 获取当前生效的所有系统配置参数
     *
     * @return 系统配置对象
     */
    @GetMapping
    public Result<SystemSettingsDTO> getSettings() {
        log.info("收到获取系统配置请求");
        SystemSettingsDTO settings = settingsService.getSettings();
        return Result.success(settings);
    }
    
    /**
     * 更新系统配置
     * 批量更新系统配置参数，仅需发送需要修改的字段
     *
     * @param settingsDTO 配置参数
     * @return 操作结果
     */
    @PostMapping
    public Result<Void> updateSettings(@RequestBody SystemSettingsDTO settingsDTO) {
        log.info("收到更新系统配置请求，参数：{}", settingsDTO);
        settingsService.updateSettings(settingsDTO);
        return Result.success(null, "系统配置已保存生效");
    }
}
