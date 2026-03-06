package com.example.aiend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应 DTO
 *
 * @author AI-End
 * @since 2025-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    
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
         * 真实姓名
         */
        private String realName;
        
        /**
         * 昵称
         */
        private String nickname;
        
        /**
         * 角色
         */
        private String role;
        
        /**
         * 手机号
         */
        private String phone;
        
        /**
         * 邮箱
         */
        private String email;
        
        /**
         * 头像
         */
        private String avatar;
    }
}
