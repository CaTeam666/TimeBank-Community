package com.example.aiend.vo;

import lombok.Data;

import java.util.List;

/**
 * 仲裁详情VO
 *
 * @author AI-End
 * @since 2025-12-26
 */
@Data
public class ArbitrationDetailVO {
    
    /**
     * 仲裁单ID
     */
    private String id;
    
    /**
     * 关联任务ID
     */
    private String taskId;
    
    /**
     * 任务标题
     */
    private String taskTitle;
    
    /**
     * 任务描述
     */
    private String taskDescription;
    
    /**
     * 任务地址
     */
    private String taskAddress;
    
    /**
     * 任务截止时间
     */
    private String taskDeadline;
    
    /**
     * 申诉发起人ID
     */
    private String initiatorId;
    
    /**
     * 申诉发起人姓名
     */
    private String initiatorName;
    
    /**
     * 申诉发起人角色（VOLUNTEER/PUBLISHER）
     */
    private String initiatorRole;
    
    /**
     * 申诉类型（如：拒不验收、虚假服务）
     */
    private String type;
    
    /**
     * 申诉详情/理由
     */
    private String description;
    
    /**
     * 被申诉人回应
     */
    private String defendantResponse;
    
    /**
     * 创建时间
     */
    private String createTime;
    
    /**
     * 状态（PENDING/APPROVED/REJECTED）
     */
    private String status;
    
    /**
     * 证据图片URL列表 (发起人)
     */
    private List<String> evidenceImages;

    /**
     * 被申诉人证据图片URL (原始字符串)
     */
    private String defendantEvidenceImg;

    /**
     * 被申诉人证据图片URL列表
     */
    private List<String> defendantEvidenceImages;
}
