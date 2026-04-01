package com.example.aiend.dto.request;

import lombok.Data;

/**
 * 系统设置DTO
 * 用于获取和更新系统配置参数
 *
 * @author AI-End
 * @since 2025-12-27
 */
@Data
public class SystemSettingsDTO {
    
    /**
     * 老人初始赠送时间币
     */
    private Integer elderInitialCoins;
    
    /**
     * 每日签到基础奖励
     */
    private Integer dailySignInReward;
    
    /**
     * 月度排行榜第一名奖金
     */
    private Integer monthlyRank1Reward;
    
    /**
     * 月度排行榜第二名奖金
     */
    private Integer monthlyRank2Reward;
    
    /**
     * 月度排行榜第三名奖金
     */
    private Integer monthlyRank3Reward;
    
    /**
     * 月度排行榜第四名奖金
     */
    private Integer monthlyRank4Reward;
    
    /**
     * 月度排行榜第五名奖金
     */
    private Integer monthlyRank5Reward;
    
    /**
     * 任务发布预扣手续费比例 (%)
     */
    private Integer transactionFeePercent;
    
    /**
     * 僵尸任务超时判定时长（小时）
     */
    private Integer zombieTaskTimeoutHours;
    
    /**
     * 任务完成自动验收期限（天）
     */
    private Integer taskAutoAcceptDays;
    
    /**
     * 老人亲属绑定数量上限（人）
     */
    private Integer familyBindingMaxLimit;
    
    /**
     * 代理人每日操作次数限制（次）
     */
    private Integer proxyDailyActionLimit;
    
    /**
     * 订单未核销自动取消时长（小时）
     */
    private Integer orderCancelTimeoutHours;
}
