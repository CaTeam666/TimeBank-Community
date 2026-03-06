package com.example.aiend.vo;

import lombok.Data;

/**
 * 仲裁列表项VO
 *
 * @author AI-End
 * @since 2025-12-26
 */
@Data
public class ArbitrationListVO {
    
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
     * 申诉类型
     */
    private String type;
    
    /**
     * 创建时间
     */
    private String createTime;
    
    /**
     * 状态（PENDING/APPROVED/REJECTED）
     */
    private String status;
}
