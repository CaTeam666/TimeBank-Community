package com.example.aiend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 僵尸任务日志实体类
 * 记录被系统自动关闭的僵尸任务及其退款状态
 *
 * @author AI-End
 * @since 2026-01-14
 */
@Data
@TableName("tb_zombie_task_log")
public class ZombieTaskLog {

    /**
     * 日志ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联任务ID
     */
    @TableField("task_id")
    private Long taskId;

    /**
     * 任务标题（快照）
     */
    @TableField("task_title")
    private String taskTitle;

    /**
     * 自动关闭时间
     */
    @TableField("closed_time")
    private LocalDateTime closedTime;

    /**
     * 自动退款金额
     */
    @TableField("refund_amount")
    private Integer refundAmount;

    /**
     * 退款状态 (SUCCESS:成功 FAILURE:失败)
     */
    @TableField("refund_status")
    private String refundStatus;

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
