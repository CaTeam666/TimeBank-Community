package com.example.aiend.dto.response.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 申诉详情响应DTO
 * 用于用户端查看任务关联的申诉信息
 *
 * @author AI-End
 * @since 2026-01-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppealDetailDTO {
    
    /**
     * 申诉ID
     */
    private String id;
    
    /**
     * 任务ID
     */
    private String taskId;
    
    /**
     * 申诉发起人ID
     */
    private String proposerId;
    
    /**
     * 申诉发起人昵称
     */
    private String proposerName;
    
    /**
     * 申诉发起人头像
     */
    private String proposerAvatar;
    
    /**
     * 申诉理由
     */
    private String reason;
    
    /**
     * 申诉发起人证据图片URL
     */
    private String evidenceImg;
    
    /**
     * 被申诉方昵称
     */
    private String defendantName;
    
    /**
     * 被申诉方头像
     */
    private String defendantAvatar;
    
    /**
     * 被申诉方回应
     */
    private String defendantResponse;
    
    /**
     * 被申诉方证据图片URL
     */
    private String defendantEvidenceImg;
    
    /**
     * 回应时间
     */
    private String responseTime;
    
    /**
     * 申诉状态（0:待处理 1:已完成 2:已驳回）
     */
    private Integer status;
    
    /**
     * 申诉时间
     */
    private String createTime;
    
    /**
     * 裁决结果
     */
    private String handlingResult;
    
    /**
     * 裁决说明
     */
    private String handlingReason;
}
