package com.example.aiend.vo;

import lombok.Data;

/**
 * 商品VO
 *
 * @author AI-End Team
 * @since 2024-12-27
 */
@Data
public class ProductVO {

    /**
     * 商品ID
     */
    private String id;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 图片URL
     */
    private String image;

    /**
     * 兑换所需时间币
     */
    private Integer price;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 状态 (ON_SHELF/OFF_SHELF)
     */
    private String status;

    /**
     * 销量
     */
    private Integer salesCount;

    /**
     * 商品分类（粮油副食/日用百货/医疗健康/虚拟券卡）
     */
    private String category;
}
