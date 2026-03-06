package com.example.aiend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 商品更新请求DTO
 *
 * @author AI-End Team
 * @since 2024-12-27
 */
@Data
public class ProductUpdateDTO {

    /**
     * 商品ID
     */
    @NotBlank(message = "商品ID不能为空")
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
    @Min(value = 1, message = "商品价格必须大于0")
    private Integer price;

    /**
     * 库存数量
     */
    @Min(value = 0, message = "库存数量不能为负数")
    private Integer stock;

    /**
     * 商品分类（粮油副食/日用百货/医疗健康/虚拟券卡）
     */
    private String category;
}
