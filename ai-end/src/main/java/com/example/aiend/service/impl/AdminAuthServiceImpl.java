package com.example.aiend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aiend.common.exception.BusinessException;
import com.example.aiend.dto.request.LoginRequestDTO;
import com.example.aiend.dto.response.LoginResponseDTO;
import com.example.aiend.entity.AdminUser;
import com.example.aiend.mapper.AdminUserMapper;
import com.example.aiend.service.AdminAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 系统端认证服务实现类
 * 实现管理后台的用户认证功能
 *
 * @author AI-End
 * @since 2026-02-27
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {
    
    private final AdminUserMapper adminUserMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 系统端 Token 前缀
     */
    private static final String ADMIN_TOKEN_PREFIX = "admin-jwt-token-";
    
    /**
     * Redis 中 Token 缓存的 Key 前缀
     */
    private static final String REDIS_TOKEN_KEY_PREFIX = "admin:token:";
    
    /**
     * Token 有效期（小时）
     */
    private static final long TOKEN_EXPIRE_HOURS = 24;
    
    /**
     * 系统端用户登录
     * 使用 username + password 进行认证
     *
     * @param loginRequest 登录请求
     * @return 登录响应（包含 token 和用户信息）
     */
    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        log.info("系统端用户登录，账号：{}", loginRequest.getUsername());
        
        // 根据用户名查询用户
        LambdaQueryWrapper<AdminUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AdminUser::getUsername, loginRequest.getUsername());
        AdminUser user = adminUserMapper.selectOne(queryWrapper);
        
        // 验证用户是否存在
        if (user == null) {
            log.warn("用户不存在，账号：{}", loginRequest.getUsername());
            throw new BusinessException(401, "账号或密码错误");
        }
        
        // 验证密码
        if (!user.getPassword().equals(loginRequest.getPassword())) {
            log.warn("密码错误，账号：{}", loginRequest.getUsername());
            throw new BusinessException(401, "账号或密码错误");
        }
        
        // 验证用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            log.warn("用户已被禁用，账号：{}", loginRequest.getUsername());
            throw new BusinessException(403, "用户已被禁用");
        }
        
        // 生成 Token：admin-jwt-token-{userId}-{timestamp}
        String token = ADMIN_TOKEN_PREFIX + user.getId() + "-" + System.currentTimeMillis();
        
        // 将 Token 存入 Redis，设置过期时间
        String redisKey = REDIS_TOKEN_KEY_PREFIX + user.getId();
        redisTemplate.opsForValue().set(redisKey, token, TOKEN_EXPIRE_HOURS, TimeUnit.HOURS);
        
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
        
        log.info("系统端用户登录成功，userId: {}, username: {}", user.getId(), user.getUsername());
        
        return response;
    }
    
    /**
     * 验证系统端 Token
     *
     * @param token 令牌
     * @return 用户ID，验证失败返回 null
     */
    @Override
    public String validateToken(String token) {
        if (token == null || !isAdminToken(token)) {
            return null;
        }
        
        try {
            // 解析 Token 获取用户ID：admin-jwt-token-{userId}-{timestamp}
            String[] parts = token.split("-");
            if (parts.length < 5) {
                log.warn("系统端 Token 格式错误：{}", token);
                return null;
            }
            
            // userId 在第4个位置（索引3）
            String userId = parts[3];
            
            // 从 Redis 获取存储的 Token 进行比对
            String redisKey = REDIS_TOKEN_KEY_PREFIX + userId;
            Object storedToken = redisTemplate.opsForValue().get(redisKey);
            
            if (storedToken == null) {
                log.warn("系统端 Token 已过期或不存在，userId：{}", userId);
                return null;
            }
            
            if (!token.equals(storedToken.toString())) {
                log.warn("系统端 Token 不匹配，可能已在其他设备登录，userId：{}", userId);
                return null;
            }
            
            return userId;
            
        } catch (Exception e) {
            log.error("系统端 Token 验证异常", e);
            return null;
        }
    }
    
    /**
     * 判断是否为系统端 Token
     *
     * @param token 令牌
     * @return 是否为系统端 Token
     */
    @Override
    public boolean isAdminToken(String token) {
        return token != null && token.startsWith(ADMIN_TOKEN_PREFIX);
    }
}
