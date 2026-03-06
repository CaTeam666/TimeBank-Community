package com.example.aiend.dto.request;

import lombok.Data;

/**
 * 实名审核查询 DTO
 *
 * @author AI-End
 * @since 2025-12-24
 */
@Data
public class IdentityAuditQueryDTO {
    
    /**
     * 页码，默认 1
     */
    private Integer page = 1;
    
    /**
     * 每页数量，默认 10
     */
    private Integer pageSize = 10;
    
    /**
     * 审核状态筛选 (PENDING, APPROVED, REJECTED)
     */
    private String status;
}
