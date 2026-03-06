package com.example.aiend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 排名奖励日志实体类
 * 记录每月自动发放的奖励记录
 *
 * @author AI-End
 * @since 2025-12-27
 */
@Data
@TableName("tb_ranking_log")
public class RankingLog {
    
    /**
     * 日志ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 期数 (例如: 2024-11)
     */
    @TableField("period")
    private String period;
    
    /**
     * 排名
     */
    @TableField("`rank`")
    private Integer rank;
    
    /**
     * 志愿者ID
     */
    @TableField("volunteer_id")
    private Long volunteerId;
    
    /**
     * 志愿者姓名
     */
    @TableField("volunteer_name")
    private String volunteerName;
    
    /**
     * 志愿者头像
     */
    @TableField("volunteer_avatar")
    private String volunteerAvatar;
    
    /**
     * 接单数 (原服务时长)
     */
    @TableField("order_count")
    private Integer orderCount;
    
    /**
     * 奖励金额
     */
    @TableField("reward_amount")
    private BigDecimal rewardAmount;
    
    /**
     * 发放时间
     */
    @TableField("distribution_time")
    private LocalDateTime distributionTime;
    
    /**
     * 状态 (SUCCESS: 成功, FAILURE: 失败)
     */
    @TableField("status")
    private String status;
    
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
