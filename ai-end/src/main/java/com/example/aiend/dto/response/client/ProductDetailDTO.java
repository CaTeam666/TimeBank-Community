package com.example.aiend.dto.response.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品详情响应DTO
 * 用于爱心超市商品详情展示
 *
 * @author AI-End
 * @since 2026-01-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailDTO {
    
    /**
     * 商品ID
     */
    private Long id;
    
    /**
     * 商品名称
     */
    private String name;
    
    /**
     * 商品图片URL
     */
    private String image;
    
    /**
     * 商品价格（积分）
     */
    private Integer price;
    
    /**
     * 商品分类
     */
    private String category;
    
    /**
     * 库存数量
     */
    private Integer stock;
    
    /**
     * 月销量
     */
    private Integer sales;
    
    /**
     * 商品描述
     */
    private String description;
}
