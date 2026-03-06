package com.example.aiend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 时间币流水实体类
 *
 * @author AI-End
 * @since 2026-01-02
 */
@Data
@TableName("tb_coin_log")
public class CoinLog {

    /**
     * 日志ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 金额（正数为收入，负数为支出）
     */
    @TableField("amount")
    private Integer amount;

    /**
     * 类型（1:任务收入 2:任务支出 3:兑换支出 4:系统调整）
     */
    @TableField("type")
    private Integer type;

    /**
     * 关联任务ID
     */
    @TableField("task_id")
    private Long taskId;

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
