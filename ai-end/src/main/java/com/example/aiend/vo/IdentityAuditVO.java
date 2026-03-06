package com.example.aiend.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 实名认证审核视图对象 VO
 *
 * @author AI-End
 * @since 2025-12-24
 */
@Data
@Builder
public class IdentityAuditVO {
    
    /**
     * 审核任务ID
     */
    private String id;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 用户名（真实姓名）
     */
    private String userName;
    
    /**
     * 提交时间
     */
    private String submitTime;
    
    /**
     * OCR识别年龄
     */
    private Integer ocrAge;
    
    /**
     * 身份证人像面图片URL
     */
    private String idCardFront;
    
    /**
     * 身份证国徽面图片URL
     */
    private String idCardBack;
    
    /**
     * OCR识别姓名
     */
    private String ocrName;
    
    /**
     * OCR识别身份证号
     */
    private String ocrIdNumber;
    
    /**
     * 审核状态 (PENDING, APPROVED, REJECTED)
     */
    private String status;
}
