package com.example.aiend.dto.request;

import lombok.Data;

/**
 * 任务查询DTO
 *
 * @author AI-End
 * @since 2025-12-26
 */
@Data
public class MissionQueryDTO {
    
    /**
     * 页码，默认1
     */
    private Integer page = 1;
    
    /**
     * 每页条数，默认10
     */
    private Integer pageSize = 10;
    
    /**
     * 搜索关键词（任务ID / 标题 / 发布人手机）
     */
    private String keyword;
    
    /**
     * 状态筛选（ALL, PENDING, IN_PROGRESS, WAITING_CONFIRM, COMPLETED, CANCELLED, APPEALING）
     */
    private String status;
    
    /**
     * 类型筛选（ALL, CHAT, CLEANING, ERRAND, MEDICAL）
     */
    private String type;
    
    /**
     * 发布日期（YYYY-MM-DD）
     */
    private String date;
}
