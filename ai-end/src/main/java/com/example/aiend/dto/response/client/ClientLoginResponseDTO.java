package com.example.aiend.dto.response.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户端登录响应 DTO
 *
 * @author AI-End
 * @since 2025-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientLoginResponseDTO {
    
    /**
     * 身份令牌
     */
    private String token;
    
    /**
     * 用户信息
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
         * 用户角色 (VOLUNTEER, SENIOR, AGENT)
         */
        private String role;
        
        /**
         * 头像URL
         */
        private String avatar;
    }
}
