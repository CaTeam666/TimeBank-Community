package com.example.aiend.service.client.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aiend.common.exception.BusinessException;
import com.example.aiend.dto.request.client.ClientLoginRequestDTO;
import com.example.aiend.dto.request.client.ClientRegisterDTO;
import com.example.aiend.dto.response.client.ClientAuditResultDTO;
import com.example.aiend.dto.response.client.ClientLoginResponseDTO;
import com.example.aiend.dto.response.client.ClientRegisterResponseDTO;
import com.example.aiend.dto.response.client.UserInfoDTO;
import com.example.aiend.entity.IdentityAudit;
import com.example.aiend.entity.User;
import com.example.aiend.mapper.IdentityAuditMapper;
import com.example.aiend.mapper.UserMapper;
import com.example.aiend.service.client.ClientAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户端认证服务实现类
 * 处理用户端的登录、注册、审核查询等认证相关业务
 *
 * @author AI-End
 * @since 2025-12-28
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ClientAuthServiceImpl implements ClientAuthService {
    
    private final UserMapper userMapper;
    private final IdentityAuditMapper identityAuditMapper;
    
    /**
     * 用户角色映射：前端角色 -> 数据库角色
     */
    private String mapRoleToDb(String role) {
        return switch (role) {
            case "SENIOR" -> "老人";
            case "VOLUNTEER" -> "志愿者";
            case "AGENT" -> "子女代理人";
            default -> throw new BusinessException(400, "无效的角色类型");
        };
    }
    
    /**
     * 数据库角色 -> 前端角色映射
     */
    private String mapRoleFromDb(String dbRole) {
        return switch (dbRole) {
            case "老人" -> "SENIOR";
            case "志愿者" -> "VOLUNTEER";
            case "子女代理人" -> "AGENT";
            default -> dbRole;
        };
    }
    
    /**
     * 用户登录
     * 通过手机号和密码验证用户身份
     *
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    @Override
    public ClientLoginResponseDTO login(ClientLoginRequestDTO loginRequest) {
        log.info("用户端登录，手机号：{}", loginRequest.getPhone());
        
        // 根据手机号查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, loginRequest.getPhone());
        User user = userMapper.selectOne(queryWrapper);
        
        // 验证用户是否存在
        if (user == null) {
            log.warn("用户不存在，手机号：{}", loginRequest.getPhone());
            throw new BusinessException(401, "手机号或密码错误");
        }
        
        // 验证密码
        if (!loginRequest.getPassword().equals(user.getPassword())) {
            log.warn("密码错误，手机号：{}", loginRequest.getPhone());
            throw new BusinessException(401, "手机号或密码错误");
        }
        
        // 验证用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            log.warn("用户已被禁用或未审核通过，手机号：{}", loginRequest.getPhone());
            throw new BusinessException(403, "账号已被禁用或实名认证未通过");
        }
        
        // 生成 Token（格式: client-jwt-token-{userId}-{timestamp}）
        String token = "client-jwt-token-" + user.getId() + "-" + System.currentTimeMillis();
        
        // 构建用户信息
        ClientLoginResponseDTO.UserInfo userInfo = ClientLoginResponseDTO.UserInfo.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .role(mapRoleFromDb(user.getRole()))
                .avatar(user.getAvatar())
                .build();
        
        // 构建登录响应
        ClientLoginResponseDTO response = ClientLoginResponseDTO.builder()
                .token(token)
                .user(userInfo)
                .build();
        
        log.info("用户端登录成功，userId: {}, phone: {}", user.getId(), user.getPhone());
        
        return response;
    }
    
    /**
     * 用户注册
     * 1. 验证手机号、昵称、身份证是否已存在
     * 2. 创建用户记录（状态为禁用，等待审核）
     * 3. 创建实名认证审核记录
     *
     * @param registerRequest 注册请求
     * @return 注册响应
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClientRegisterResponseDTO register(ClientRegisterDTO registerRequest) {
        log.info("用户端注册，手机号：{}", registerRequest.getPhone());
        
        // 验证手机号是否已存在
        LambdaQueryWrapper<User> phoneQuery = new LambdaQueryWrapper<>();
        phoneQuery.eq(User::getPhone, registerRequest.getPhone());
        if (userMapper.selectCount(phoneQuery) > 0) {
            throw new BusinessException(400, "该手机号已被注册");
        }
        
        // 验证昵称是否已存在
        LambdaQueryWrapper<User> nicknameQuery = new LambdaQueryWrapper<>();
        nicknameQuery.eq(User::getNickname, registerRequest.getNickname());
        if (userMapper.selectCount(nicknameQuery) > 0) {
            throw new BusinessException(400, "该昵称已被使用");
        }
        
        // 验证身份证号是否已存在
        LambdaQueryWrapper<User> idCardQuery = new LambdaQueryWrapper<>();
        idCardQuery.eq(User::getIdCard, registerRequest.getIdCard());
        if (userMapper.selectCount(idCardQuery) > 0) {
            throw new BusinessException(400, "该身份证号已被注册");
        }
        
        // 验证真实姓名是否已存在
        LambdaQueryWrapper<User> realNameQuery = new LambdaQueryWrapper<>();
        realNameQuery.eq(User::getRealName, registerRequest.getRealName());
        if (userMapper.selectCount(realNameQuery) > 0) {
            throw new BusinessException(400, "该真实姓名已被注册");
        }
        
        // 根据身份证号计算年龄并校验身份
        int age = 0;
        try {
            String birthYear = registerRequest.getIdCard().substring(6, 10);
            age = LocalDateTime.now().getYear() - Integer.parseInt(birthYear);
        } catch (Exception e) {
            log.warn("计算年龄失败，idCard: {}", registerRequest.getIdCard());
        }
        
        // 年龄校验：不满60岁不能以老人身份注册
        if ("SENIOR".equals(registerRequest.getRole()) && age < 60) {
            throw new BusinessException(400, "不满60岁不能以老人身份注册");
        }
        
        // 创建用户记录（状态为禁用，等待审核通过后启用）
        User user = new User();
        user.setPhone(registerRequest.getPhone());
        user.setPassword(registerRequest.getPassword());
        user.setNickname(registerRequest.getNickname());
        user.setRealName(registerRequest.getRealName());
        user.setIdCard(registerRequest.getIdCard());
        user.setRole(mapRoleToDb(registerRequest.getRole()));
        user.setStatus(0);  // 禁用状态，等待审核
        user.setBalance(0);
        user.setCreateTime(LocalDateTime.now());
        user.setRegisterTime(LocalDateTime.now());
        user.setDeleted(0);
        
        userMapper.insert(user);
        log.info("创建用户记录成功，userId: {}", user.getId());
        
        // 创建实名认证审核记录
        IdentityAudit audit = new IdentityAudit();
        audit.setUserId(Long.parseLong(user.getId()));
        audit.setRealName(registerRequest.getRealName());
        audit.setIdCard(registerRequest.getIdCard());
        audit.setIdCardFront(registerRequest.getIdCardFront());
        audit.setIdCardBack(registerRequest.getIdCardBack());
        audit.setStatus(0);  // 待审核
        audit.setAge(age);
        audit.setCreateTime(LocalDateTime.now());
        audit.setIsDeleted(0);
        
        identityAuditMapper.insert(audit);
        log.info("创建审核记录成功，auditId: {}, userId: {}", audit.getId(), user.getId());
        
        // 返回注册响应
        return ClientRegisterResponseDTO.builder()
                .auditId(String.valueOf(audit.getId()))
                .status(0)
                .build();
    }
    
    /**
     * 查询审核结果
     * 1. 根据审核ID查询审核记录
     * 2. 审核通过时：启用用户账号，返回token和用户信息
     * 3. 审核驳回时：返回驳回原因
     * 4. 待审核时：仅返回状态
     *
     * @param auditId 审核ID
     * @return 审核结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClientAuditResultDTO getAuditResult(String auditId) {
        log.info("查询审核结果，auditId: {}", auditId);
        
        // 查询审核记录
        IdentityAudit audit = identityAuditMapper.selectById(Long.parseLong(auditId));
        if (audit == null) {
            throw new BusinessException(404, "审核记录不存在");
        }
        
        // 根据审核状态处理
        if (audit.getStatus() == 0) {
            // 待审核
            return ClientAuditResultDTO.builder()
                    .status(0)
                    .build();
        } else if (audit.getStatus() == 2) {
            // 已驳回
            return ClientAuditResultDTO.builder()
                    .status(2)
                    .rejectReason(audit.getRejectReason())
                    .build();
        } else if (audit.getStatus() == 1) {
            // 已通过，需要启用用户账号并返回token
            User user = userMapper.selectById(String.valueOf(audit.getUserId()));
            if (user == null) {
                throw new BusinessException(500, "用户数据异常");
            }
            
            // 如果用户状态仍为禁用，则启用
            if (user.getStatus() == 0) {
                user.setStatus(1);
                user.setUpdateTime(LocalDateTime.now());
                userMapper.updateById(user);
                log.info("用户账号启用成功，userId: {}", user.getId());
            }
            
            // 生成 Token（格式: client-jwt-token-{userId}-{timestamp}）
            String token = "client-jwt-token-" + user.getId() + "-" + System.currentTimeMillis();
            
            // 构建用户信息
            ClientAuditResultDTO.UserInfo userInfo = ClientAuditResultDTO.UserInfo.builder()
                    .id(user.getId())
                    .phone(user.getPhone())
                    .nickname(user.getNickname())
                    .role(mapRoleFromDb(user.getRole()))
                    .avatar(user.getAvatar())
                    .build();
            
            return ClientAuditResultDTO.builder()
                    .status(1)
                    .token(token)
                    .user(userInfo)
                    .build();
        }
        
        // 未知状态
        throw new BusinessException(500, "审核状态异常");
    }
    
    /**
     * 获取用户信息
     * 根据用户ID查询用户详细信息（包含状态、余额等）
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @Override
    public UserInfoDTO getUserInfo(String userId) {
        log.info("获取用户信息，userId：{}", userId);
        
        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        
        // 构建响应DTO
        return UserInfoDTO.builder()
                .id(user.getId())
                .username(user.getPhone())  // 用户名（手机号）
                .nickname(user.getNickname())
                .realName(user.getRealName())
                .role(mapRoleFromDb(user.getRole()))  // 转换为前端角色格式
                .status(user.getStatus())
                .balance(user.getBalance())
                .avatar(user.getAvatar())
                .build();
    }
}
