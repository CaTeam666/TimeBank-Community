package com.example.aiend.dto.response.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 任务详情响应 DTO
 * 用于 Redis 缓存和前端展示
 *
 * @author AI-End
 * @since 2025-12-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDetailResponseDTO implements Serializable {
    
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
     * 任务状态（0:待接取 1:进行中 2:待确认 3:已完成 4:已取消）
     */
    private Integer status;
    
    /**
     * 发布者ID
     */
    private String publisherId;
    
    /**
     * 发布者昵称
     */
    private String publisherName;
    
    /**
     * 发布者头像
     */
    private String publisherAvatar;
}
