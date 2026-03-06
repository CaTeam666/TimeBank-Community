package com.example.aiend.service;

import com.example.aiend.dto.request.LoginRequestDTO;
import com.example.aiend.dto.response.LoginResponseDTO;

/**
 * 认证服务接口
 *
 * @author AI-End
 * @since 2025-12-19
 */
public interface AuthService {
    
    /**
     * 用户登录
     *
     * @param loginRequest 登录请求
     * @return 登录响应（包含 token 和用户信息）
     */
    LoginResponseDTO login(LoginRequestDTO loginRequest);
}
