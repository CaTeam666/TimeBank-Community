package com.example.aiend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务实体类
 *
 * @author AI-End
 * @since 2025-12-26
 */
@Data
@TableName("tb_task")
public class Task {
    
    /**
     * 任务ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 任务标题
     */
    @TableField("title")
    private String title;
    
    /**
     * 任务描述
     */
    @TableField("description")
    private String description;
    
    /**
     * 任务类型（CHAT, CLEANING, ERRAND, MEDICAL）
     */
    @TableField("type")
    private String type;
    
    /**
     * 任务报酬（时间币）
     */
    @TableField("price")
    private Integer price;
    
    /**
     * 发布者ID
     */
    @TableField("publisher_id")
    private Long publisherId;
    
    /**
     * 代理人ID（子女代理发布时记录，非代理发布为null）
     */
    @TableField("proxy_user_id")
    private Long proxyUserId;
    
    /**
     * 志愿者ID
     */
    @TableField("volunteer_id")
    private Long volunteerId;
    
    /**
     * 任务状态（0:待接取 1:进行中 2:待确认 3:已完成 4:申诉中 5:已取消）
     */
    @TableField("status")
    private Integer status;
    
    /**
     * 取消/关闭原因
     */
    @TableField("cancel_reason")
    private String cancelReason;
    
    /**
     * 完成证明图片URL
     */
    @TableField("proof_img")
    private String proofImg;
    
    /**
     * 服务地址
     */
    @TableField("location")
    private String location;
    
    /**
     * 服务时间
     */
    @TableField("service_time")
    private LocalDateTime serviceTime;
    
    /**
     * 截止时间
     */
    @TableField("deadline")
    private LocalDateTime deadline;
    
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
     * 逻辑删除标记（0:未删除 1:已删除）
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
