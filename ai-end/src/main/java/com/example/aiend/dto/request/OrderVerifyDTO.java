package com.example.aiend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 订单核销请求DTO
 *
 * @author AI-End Team
 * @since 2024-12-27
 */
@Data
public class OrderVerifyDTO {

    /**
     * 订单ID（id和verifyCode二选一）
     */
    private String id;

    /**
     * 核销码（id和verifyCode二选一）
     */
    private String verifyCode;
}
