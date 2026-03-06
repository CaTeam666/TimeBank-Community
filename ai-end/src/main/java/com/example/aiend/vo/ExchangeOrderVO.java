package com.example.aiend.vo;

import lombok.Data;

/**
 * 订单VO
 *
 * @author AI-End Team
 * @since 2024-12-27
 */
@Data
public class ExchangeOrderVO {

    /**
     * 订单ID
     */
    private String id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 志愿者ID
     */
    private String volunteerId;

    /**
     * 志愿者姓名
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
