package com.example.aiend.service.client;

import com.example.aiend.dto.request.client.ClientLoginRequestDTO;
import com.example.aiend.dto.request.client.ClientRegisterDTO;
import com.example.aiend.dto.response.client.ClientAuditResultDTO;
import com.example.aiend.dto.response.client.ClientLoginResponseDTO;
import com.example.aiend.dto.response.client.ClientRegisterResponseDTO;
import com.example.aiend.dto.response.client.UserInfoDTO;

/**
 * 用户端认证服务接口
 * 处理用户端的登录、注册、审核查询等认证相关业务
 *
 * @author AI-End
 * @since 2025-12-28
 */
public interface ClientAuthService {
    
    /**
     * 用户登录
     *
     * @param loginRequest 登录请求（手机号+密码）
     * @return 登录响应（包含 token 和用户信息）
     */
    ClientLoginResponseDTO login(ClientLoginRequestDTO loginRequest);
    
    /**
     * 用户注册
     * 提交实名认证信息，创建审核记录
     *
     * @param registerRequest 注册请求（包含手机号、密码、实名信息、身份证照片等）
     * @return 注册响应（包含审核ID和初始状态）
     */
    ClientRegisterResponseDTO register(ClientRegisterDTO registerRequest);
    
    /**
     * 查询审核结果
     * 根据审核ID查询实名认证审核状态
     * 如果审核通过，将用户信息存入系统用户表并返回token
     *
     * @param auditId 审核ID
     * @return 审核结果（包含状态、驳回原因、token、用户信息）
     */
    ClientAuditResultDTO getAuditResult(String auditId);
    
    /**
     * 获取用户信息
     * 根据用户ID查询用户详细信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserInfoDTO getUserInfo(String userId);
}
