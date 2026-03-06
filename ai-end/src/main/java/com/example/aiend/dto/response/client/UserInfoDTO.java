package com.example.aiend.dto.response.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户信息响应DTO
 * 用于返回当前用户的详细信息
 *
 * @author AI-End
 * @since 2026-01-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {
    
    /**
     * 用户ID
     */
    private String id;
    
    /**
     * 用户名（手机号）
     */
    private String username;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 角色
     */
    private String role;
    
    /**
     * 状态（0:禁用 1:启用）
     */
    private Integer status;
    
    /**
     * 余额
     */
    private Integer balance;
    
    /**
     * 头像URL
     */
    private String avatar;
}
