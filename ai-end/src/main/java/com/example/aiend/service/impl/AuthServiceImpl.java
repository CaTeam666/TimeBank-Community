package com.example.aiend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aiend.common.exception.BusinessException;
import com.example.aiend.dto.request.LoginRequestDTO;
import com.example.aiend.dto.response.LoginResponseDTO;
import com.example.aiend.entity.User;
import com.example.aiend.mapper.UserMapper;
import com.example.aiend.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 认证服务实现类
 *
 * @author AI-End
 * @since 2025-12-19
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final UserMapper userMapper;
    
    /**
     * 用户登录
     * 使用手机号作为登录账号，密码验证暂时省略（因为sys_user表没有password字段）
     *
     * @param loginRequest 登录请求
     * @return 登录响应（包含 token 和用户信息）
     */
    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        log.info("用户登录，账号：{}", loginRequest.getUsername());
        
        // 根据手机号查询用户（username字段实际对应phone）
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, loginRequest.getUsername());
        User user = userMapper.selectOne(queryWrapper);
        
        // 验证用户是否存在
        if (user == null) {
            log.warn("用户不存在，账号：{}", loginRequest.getUsername());
            throw new BusinessException(401, "账号或密码错误");
        }
        
        // 验证用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            log.warn("用户已被禁用，账号：{}", loginRequest.getUsername());
            throw new BusinessException(403, "用户已被禁用");
        }
        
        // 生成 Token（模拟 JWT token）
        String token = "mock-jwt-token-" + System.currentTimeMillis();
        
        // 构建用户信息
        LoginResponseDTO.UserInfo userInfo = LoginResponseDTO.UserInfo.builder()
                .id(user.getId())
                .realName(user.getRealName())
                .nickname(user.getNickname())
                .role(user.getRole())
                .phone(user.getPhone())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .build();
        
        // 构建登录响应
        LoginResponseDTO response = LoginResponseDTO.builder()
                .token(token)
                .user(userInfo)
                .build();
        
        log.info("用户登录成功，userId: {}, phone: {}", user.getId(), user.getPhone());
        
        return response;
    }
}
