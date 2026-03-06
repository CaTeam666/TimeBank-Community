package com.example.aiend.dto.request;

import lombok.Data;

/**
 * 订单操作请求DTO
 * 用于订单核销和取消操作
 *
 * @author AI-End
 * @since 2026-01-13
 */
@Data
public class OrderOperateDTO {
    
    /**
     * 订单ID
     */
    private String id;
    
    /**
     * 核销码（可选，核销时可用）
     */
    private String verifyCode;
}
