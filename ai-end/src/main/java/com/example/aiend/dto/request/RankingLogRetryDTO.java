package com.example.aiend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 排名奖励补发DTO
 *
 * @author AI-End
 * @since 2025-12-27
 */
@Data
public class RankingLogRetryDTO {
    
    /**
     * 日志ID
     */
    @NotNull(message = "日志ID不能为空")
    private Long id;
}
