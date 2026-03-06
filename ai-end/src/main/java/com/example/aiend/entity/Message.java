package com.example.aiend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系统消息实体类
 * 统一存储所有类型的待办消息
 *
 * @author AI-End
 * @since 2026-02-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_message")
public class Message {
    
    /**
     * 消息ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 接收人ID
     */
    @TableField("receiver_id")
    private Long receiverId;
    
    /**
     * 消息类型（FAMILY_BIND:亲情绑定, TASK_VERIFY:任务验收）
     */
    @TableField("type")
    private String type;
    
    /**
     * 业务ID（关联的具体数据主键）
     */
    @TableField("biz_id")
    private Long bizId;
    
    /**
     * 消息标题
     */
    @TableField("title")
    private String title;
    
    /**
     * 消息内容
     */
    @TableField("content")
    private String content;
    
    /**
     * 消息状态（0:待处理 1:已处理）
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
    @TableField("is_deleted")
    private Integer isDeleted;
}
