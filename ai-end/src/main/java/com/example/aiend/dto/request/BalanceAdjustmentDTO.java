package com.example.aiend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 余额调整 DTO
 *
 * @author AI-End
 * @since 2025-12-24
 */
@Data
public class BalanceAdjustmentDTO {
    
    /**
     * 调整金额（正数为增加，负数为减少）
     */
    @NotNull(message = "调整金额不能为空")
    private Integer amount;
    
    /**
     * 调整原因
     */
    private String reason;
}
