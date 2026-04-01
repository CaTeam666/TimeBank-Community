package com.example.aiend.controller;

import com.example.aiend.common.Result;
import com.example.aiend.common.exception.BusinessException;
import com.example.aiend.config.interceptor.AdminAuthInterceptor;
import com.example.aiend.dto.request.SystemSettingsDTO;
import com.example.aiend.dto.request.UpdateSettingsRequestDTO;
import com.example.aiend.entity.AdminUser;
import com.example.aiend.mapper.AdminUserMapper;
import com.example.aiend.service.SettingsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
    private final AdminUserMapper adminUserMapper;
    
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
     * 批量更新系统配置参数，需要当前登录用户的密码验证
     *
     * @param requestDTO 包含密码和配置参数的请求对象
     * @param request HTTP请求，用于获取当前登录用户ID
     * @return 操作结果
     */
    @PostMapping
    public Result<Void> updateSettings(@Valid @RequestBody UpdateSettingsRequestDTO requestDTO,
                                        HttpServletRequest request) {
        log.info("收到更新系统配置请求");
        
        // 获取当前登录用户ID（由拦截器设置）
        String currentUserId = (String) request.getAttribute(AdminAuthInterceptor.ATTR_ADMIN_USER_ID);
        if (currentUserId == null) {
            log.error("无法获取当前登录用户ID");
            throw new BusinessException("请先登录");
        }
        
        // 根据当前登录用户ID查询用户信息
        AdminUser adminUser = adminUserMapper.selectById(currentUserId);
        if (adminUser == null) {
            log.error("当前登录用户不存在，userId：{}", currentUserId);
            throw new BusinessException("用户不存在");
        }
        
        // 验证当前登录用户的密码
        if (!adminUser.getPassword().equals(requestDTO.getPassword())) {
            log.warn("密码验证失败，userId：{}", currentUserId);
            throw new BusinessException("密码错误");
        }
        
        settingsService.updateSettings(requestDTO.getSettings());
        return Result.success(null, "系统配置已保存生效");
    }
}
