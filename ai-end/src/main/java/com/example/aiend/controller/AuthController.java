package com.example.aiend.controller;

import com.example.aiend.common.Result;
import com.example.aiend.dto.request.LoginRequestDTO;
import com.example.aiend.dto.response.LoginResponseDTO;
import com.example.aiend.service.AdminAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统端认证控制器
 * 处理管理后台用户登录请求
 *
 * @author AI-End
 * @since 2025-12-19
 */
@RestController
@RequestMapping("/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthController {
    
    private final AdminAuthService adminAuthService;
    
    /**
     * 系统端用户登录
     * 使用 username + password 进行认证
     *
     * @param loginRequest 登录请求（包含用户名和密码）
     * @return 登录响应（包含 token 和用户信息）
     */
    @PostMapping("/login")
    public Result<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        log.info("收到系统端登录请求，username: {}", loginRequest.getUsername());
        LoginResponseDTO response = adminAuthService.login(loginRequest);
        return Result.success(response);
    }
}
