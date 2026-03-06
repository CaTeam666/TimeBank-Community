package com.example.aiend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单列表响应DTO
 * 用于系统端订单核销列表展示
 *
 * @author AI-End
 * @since 2026-01-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderListVO {
    
    /**
     * 订单ID
     */
    private String id;
    
    /**
     * 订单编号
     */
    private String orderNo;
    
    /**
     * 志愿者ID
     */
    private String volunteerId;
    
    /**
     * 志愿者名称
     */
    private String volunteerName;
    
    /**
     * 商品ID
     */
    private String productId;
    
    /**
     * 商品名称
     */
    private String productName;
    
    /**
     * 商品图片
     */
    private String productImage;
    
    /**
     * 消耗时间币
     */
    private Integer cost;
    
    /**
     * 创建时间
     */
    private String createTime;
    
    /**
     * 核销码
     */
    private String verifyCode;
    
    /**
     * 状态 (PENDING/COMPLETED/CANCELLED)
     */
    private String status;
}
