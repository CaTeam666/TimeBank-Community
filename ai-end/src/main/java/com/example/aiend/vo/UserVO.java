package com.example.aiend.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 用户视图对象 VO
 *
 * @author AI-End
 * @since 2025-12-21
 */
@Data
@Builder
public class UserVO {
    
    /**
     * 用户唯一标识
     */
    private String id;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 用户角色
     */
    private String role;
    
    /**
     * 时间币余额
     */
    private Integer balance;
    
    /**
     * 注册时间
     */
    private String registerTime;
    
    /**
     * 账号状态
     */
    private String status;
}
