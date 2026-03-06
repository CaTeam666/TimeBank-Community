package com.example.aiend.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 排名奖励日志查询DTO
 *
 * @author AI-End
 * @since 2025-12-27
 */
@Data
public class RankingLogQueryDTO {
    
    /**
     * 期数筛选 (可选, 例如: "2024-11")
     */
    private String period;
    
    /**
     * 页码
     */
    @Min(value = 1, message = "页码最小值为1")
    private Integer page = 1;
    
    /**
     * 每页数量
     */
    @Min(value = 1, message = "每页数量最小值为1")
    private Integer pageSize = 10;
}
