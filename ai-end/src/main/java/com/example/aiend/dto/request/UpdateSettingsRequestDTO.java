package com.example.aiend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新系统设置请求DTO
 * 包装管理员密码和要更新的配置对象
 *
 * @author AI-End
 * @since 2026-02-27
 */
@Data
public class UpdateSettingsRequestDTO {
    
    /**
     * 管理员密码（用于安全验证）
     */
    @NotBlank(message = "管理员密码不能为空")
    private String password;
    
    /**
     * 要更新的系统配置
     */
    @NotNull(message = "配置参数不能为空")
    @Valid
    private SystemSettingsDTO settings;
}
