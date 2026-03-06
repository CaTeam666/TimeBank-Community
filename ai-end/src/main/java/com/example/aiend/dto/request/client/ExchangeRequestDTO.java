package com.example.aiend.dto.request.client;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 积分兑换请求DTO
 * 用于爱心超市商品兑换
 *
 * @author AI-End
 * @since 2026-01-13
 */
@Data
public class ExchangeRequestDTO {
    
    /**
     * 用户ID
     */
    @NotBlank(message = "用户ID不能为空")
    private String userId;
    
    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    private Long productId;
    
    /**
     * 兑换数量
     */
    @NotNull(message = "兑换数量不能为空")
    @Min(value = 1, message = "兑换数量至少为1")
    private Integer quantity;
}
