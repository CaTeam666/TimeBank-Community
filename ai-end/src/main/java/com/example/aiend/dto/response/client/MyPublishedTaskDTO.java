package com.example.aiend.dto.response.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 我的发布任务项 DTO
 * 用户发布的任务列表
 *
 * @author AI-End
 * @since 2025-12-31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyPublishedTaskDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 任务ID
     */
    private String taskId;
    
    /**
     * 任务标题
     */
    private String title;
    
    /**
     * 服务类型
     */
    private String type;
    
    /**
     * 任务描述
     */
    private String description;
    
    /**
     * 悬赏时间币
     */
    private Integer coins;
    
    /**
     * 服务地址
     */
    private String location;
    
    /**
     * 服务日期
     */
    private String date;
    
    /**
     * 服务时间段
     */
    private String timeRange;
    
    /**
     * 任务状态（0:待接取 1:进行中 2:待确认 3:已完成）
     */
    private Integer status;
    
    /**
     * 接单志愿者ID（可为空）
     */
    private String volunteerId;
    
    /**
     * 接单志愿者昵称（可为空）
     */
    private String volunteerName;
    
    /**
     * 接单志愿者头像（可为空）
     */
    private String volunteerAvatar;
}
