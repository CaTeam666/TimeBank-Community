package com.example.aiend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 兑换订单实体类
 *
 * @author AI-End Team
 * @since 2024-12-27
 */
@Data
@TableName("tb_exchange_order")
public class ExchangeOrder {

    /**
     * 订单ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单编号
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 商品ID
     */
    @TableField("product_id")
    private Long productId;

    /**
     * 消耗时间币
     */
    @TableField("amount")
    private Integer amount;

    /**
     * 核销码
     */
    @TableField("verify_code")
    private String verifyCode;

    /**
     * 订单状态（0:待核销 1:已核销 2:已取消）
     */
    @TableField("status")
    private Integer status;

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
