package com.example.aiend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务评价实体类
 *
 * @author AI-End
 * @since 2026-01-02
 */
@Data
@TableName("tb_task_review")
public class TaskReview {

    /**
     * 评价ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID（唯一，一个任务只能有一条评价）
     */
    @TableField("task_id")
    private Long taskId;

    /**
     * 发布者ID（评价人）
     */
    @TableField("publisher_id")
    private Long publisherId;

    /**
     * 志愿者ID（被评价人）
     */
    @TableField("volunteer_id")
    private Long volunteerId;

    /**
     * 评分（1-5星）
     */
    @TableField("rating")
    private Integer rating;

    /**
     * 评价内容
     */
    @TableField("content")
    private String content;

    /**
     * 评价时间
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
