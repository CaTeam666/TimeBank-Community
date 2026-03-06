package com.example.aiend.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 我的兑换订单VO
 * 用于返回用户的兑换订单列表信息
 *
 * @author AI-End Team
 * @since 2026-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyExchangeOrderVO {

    /**
     * 订单编号/ID
     */
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品图片
     */
    private String productImage;

    /**
     * 商品单价
     */
    private Integer price;

    /**
     * 兑换数量
     */
    private Integer quantity;

    /**
     * 总价
     */
    private Integer totalPrice;

    /**
     * 状态 (0:待核销 1:已核销 2:已取消)
     */
    private Integer status;

    /**
     * 核销码
     */
    private String verifyCode;

    /**
     * 创建时间
     */
    private String createTime;
}
