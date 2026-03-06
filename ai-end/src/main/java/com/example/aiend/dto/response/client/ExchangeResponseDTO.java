package com.example.aiend.dto.response.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 积分兑换响应DTO
 * 用于返回兑换订单信息
 *
 * @author AI-End
 * @since 2026-01-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeResponseDTO {
    
    /**
     * 订单编号
     */
    private String orderNo;
    
    /**
     * 核销码
     */
    private String verifyCode;
}
