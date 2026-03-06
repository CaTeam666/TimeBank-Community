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
     * 任务发布预扣手续费比例 (%)
     */
    private Integer transactionFeePercent;
}
