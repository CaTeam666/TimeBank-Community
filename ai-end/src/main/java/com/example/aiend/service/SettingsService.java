package com.example.aiend.service;

import com.example.aiend.dto.request.SystemSettingsDTO;

/**
 * 系统设置服务接口
 *
 * @author AI-End
 * @since 2025-12-27
 */
public interface SettingsService {
    
    /**
     * 获取所有系统配置
     *
     * @return 系统配置对象
     */
    SystemSettingsDTO getSettings();
    
    /**
     * 更新系统配置
     *
     * @param settingsDTO 配置参数
     */
    void updateSettings(SystemSettingsDTO settingsDTO);
}
