package com.example.aiend.dto.response.client;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 验收详情响应 DTO
 *
 * @author AI-End
 * @since 2026-01-02
 */
@Data
@Builder
public class ReviewDetailDTO {
    
    /**
     * 任务ID
     */
    private String taskId;
    
    /**
     * 任务标题
     */
    private String title;
    
    /**
     * 任务描述
     */
    private String description;
    
    /**
     * 悬赏时间币
     */
    private Integer coins;
    
    /**
     * 任务状态
     */
    private Integer status;
    
    /**
     * 服务凭证图片URL列表
     */
    private List<String> evidencePhotos;
    
    /**
     * 签到时间 (ISO格式)
     */
    private String checkInTime;
    
    /**
     * 完成时间 (ISO格式)
     */
    private String finishTime;
    
    /**
     * 志愿者ID
     */
    private String volunteerId;
    
    /**
     * 志愿者昵称
     */
    private String volunteerName;
    
    /**
     * 志愿者头像URL
     */
    private String volunteerAvatar;
}
