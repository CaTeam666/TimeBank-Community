package com.example.aiend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 服务存证实体类
 * 
 * @author AI-End Team
 * @since 2024-12-26
 */
@Data
@TableName("tb_task_evidence")
public class TaskEvidence {

    /**
     * 凭证ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联任务ID
     */
    @TableField("task_id")
    private Long taskId;

    /**
     * 志愿者ID
     */
    @TableField("volunteer_id")
    private Long volunteerId;

    /**
     * 凭证图片URL
     */
    @TableField("image_url")
    private String imageUrl;
    
    /**
     * 志愿者签到信息
     */
    @TableField("check_in_info")
    private String checkInInfo;
    
    /**
     * 签到时间
     */
    @TableField("check_in_time")
    private LocalDateTime checkInTime;

    /**
     * 上传时间
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
