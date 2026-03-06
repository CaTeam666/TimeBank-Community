package com.example.aiend.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 排名奖励日志视图对象
 *
 * @author AI-End
 * @since 2025-12-27
 */
@Data
public class RankingLogVO {
    
    /**
     * 日志ID
     */
    private Long id;
    
    /**
     * 期数 (例如: "2024-11")
     */
    private String period;
    
    /**
     * 排名
     */
    private Integer rank;
    
    /**
     * 志愿者ID
     */
    private Long volunteerId;
    
    /**
     * 志愿者姓名
     */
    private String volunteerName;
    
    /**
     * 志愿者头像
     */
    private String volunteerAvatar;
    
    /**
     * 接单数 (原服务时长)
     */
    private Integer orderCount;
    
    /**
     * 奖励金额
     */
    private BigDecimal rewardAmount;
    
    /**
     * 发放时间
     */
    private LocalDateTime distributionTime;
    
    /**
     * 状态 (SUCCESS: 成功, FAILURE: 失败)
     */
    private String status;
}
