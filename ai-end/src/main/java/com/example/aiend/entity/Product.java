package com.example.aiend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品实体类
 *
 * @author AI-End Team
 * @since 2024-12-27
 */
@Data
@TableName("tb_product")
public class Product {

    /**
     * 商品ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品名称
     */
    @TableField("name")
    private String name;

    /**
     * 商品描述
     */
    @TableField("description")
    private String description;

    /**
     * 商品分类（粮油副食/日用百货/医疗健康/虚拟券卡）
     */
    @TableField("category")
    private String category;

    /**
     * 商品价格（时间币）
     */
    @TableField("price")
    private Integer price;

    /**
     * 库存数量
     */
    @TableField("stock")
    private Integer stock;

    /**
     * 商品状态（1:上架 0:下架）
     */
    @TableField("status")
    private Integer status;

    /**
     * 销量
     */
    @TableField("sales_count")
    private Integer salesCount;

    /**
     * 商品图片URL
     */
    @TableField("image")
    private String image;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除（0:未删除 1:已删除）
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
