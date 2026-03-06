package com.example.aiend.dto.response.client;

import lombok.Builder;
import lombok.Data;

/**
 * 待审核绑定申请 VO
 *
 * @author AI-End
 * @since 2026-02-07
 */
@Data
@Builder
public class PendingRequestVO {
    
    /**
     * 关系记录ID
     */
    private Long relationId;
    
    /**
     * 申请人(子女)用户ID
     */
    private Long childId;
    
    /**
     * 申请人姓名
     */
    private String childName;
    
    /**
     * 申请人头像URL
     */
    private String childAvatar;
    
    /**
     * 申请人手机号(脱敏)
     */
    private String childPhone;
    
    /**
     * 关系类型
     */
    private String relation;
    
    /**
     * 证明材料图片URL
     */
    private String proofImg;
    
    /**
     * 申请时间
     */
    private String createTime;
}
