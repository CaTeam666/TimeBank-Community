package com.example.aiend.dto.response.client;

import lombok.Builder;
import lombok.Data;

/**
 * 亲情账号列表响应 VO
 *
 * @author AI-End
 * @since 2026-02-07
 */
@Data
@Builder
public class FamilyMemberVO {
    
    /**
     * 长者用户ID
     */
    private String id;
    
    /**
     * 关系记录ID
     */
    private Long relationId;
    
    /**
     * 长者昵称
     */
    private String nickname;
    
    /**
     * 长者头像URL
     */
    private String avatar;
    
    /**
     * 长者积分余额
     */
    private Integer balance;
    
    /**
     * 关系类型
     */
    private String relation;
    
    /**
     * 长者手机号(脱敏)
     */
    private String phone;
    
    /**
     * 关系状态(1:已通过)
     */
    private Integer status;
}
