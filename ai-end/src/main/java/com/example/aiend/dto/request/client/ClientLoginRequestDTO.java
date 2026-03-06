package com.example.aiend.dto.request.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 用户端登录请求 DTO
 *
 * @author AI-End
 * @since 2025-12-28
 */
@Data
public class ClientLoginRequestDTO {
    
    /**
     * 手机号（11位）
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}
