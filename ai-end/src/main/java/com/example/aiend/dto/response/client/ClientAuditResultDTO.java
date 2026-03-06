package com.example.aiend.dto.response.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户端审核结果响应 DTO
 *
 * @author AI-End
 * @since 2025-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientAuditResultDTO {
    
    /**
     * 审核状态 (0:待审核 1:已通过 2:已驳回)
     */
    private Integer status;
    
    /**
     * 驳回原因（当 status=2 时返回）
     */
    private String rejectReason;
    
    /**
     * 登录Token（当 status=1 时返回）
     */
    private String token;
    
    /**
     * 用户信息（当 status=1 时返回）
     */
    private UserInfo user;
    
    /**
     * 用户信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        
        /**
         * 用户ID
         */
        private String id;
        
        /**
         * 手机号
         */
        private String phone;
        
        /**
         * 昵称
         */
        private String nickname;
        
        /**
         * 用户角色
         */
        private String role;
        
        /**
         * 头像URL
         */
        private String avatar;
    }
}
