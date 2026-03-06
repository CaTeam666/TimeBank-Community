package com.example.aiend.dto.request;

import lombok.Data;

/**
 * 仲裁查询DTO
 *
 * @author AI-End
 * @since 2025-12-26
 */
@Data
public class ArbitrationQueryDTO {
    
    /**
     * 页码，默认1
     */
    private Integer page = 1;
    
    /**
     * 每页条数，默认10
     */
    private Integer pageSize = 10;
    
    /**
     * 状态筛选（ALL, PENDING, RESOLVED）
     */
    private String status;
    
    /**
     * 搜索关键词（ID/任务标题/发起人）
     */
    private String keyword;
}
