package com.example.aiend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 订单取消请求DTO
 *
 * @author AI-End Team
 * @since 2024-12-27
 */
@Data
public class OrderCancelDTO {

    /**
     * 订单ID
     */
    @NotBlank(message = "订单ID不能为空")
    private String id;
}
