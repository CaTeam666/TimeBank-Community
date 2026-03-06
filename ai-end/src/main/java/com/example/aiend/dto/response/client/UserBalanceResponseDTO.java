package com.example.aiend.dto.response.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户余额响应 DTO
 *
 * @author AI-End
 * @since 2025-12-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBalanceResponseDTO {
    
    /**
     * 当前余额（时间币）
     */
    private Integer balance;
}
