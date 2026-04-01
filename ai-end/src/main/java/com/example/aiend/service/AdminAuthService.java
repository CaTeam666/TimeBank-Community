package com.example.aiend.service;

import com.example.aiend.dto.request.LoginRequestDTO;
import com.example.aiend.dto.response.LoginResponseDTO;

/**
 * 系统端认证服务接口
 * 专用于管理后台的用户认证
 *
 * @author AI-End
 * @since 2026-02-27
 */
public interface AdminAuthService {
    
    /**
     * 系统端用户登录
     * 使用 username + password 进行认证
     *
     * @param loginRequest 登录请求
     * @return 登录响应（包含 token 和用户信息）
     */
    LoginResponseDTO login(LoginRequestDTO loginRequest);
    
    /**
     * 验证系统端 Token
     *
     * @param token 令牌
     * @return 用户ID，验证失败返回 null
     */
    String validateToken(String token);
    
    /**
     * 判断是否为系统端 Token
     *
     * @param token 令牌
     * @return 是否为系统端 Token
     */
    boolean isAdminToken(String token);
}
