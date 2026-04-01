package com.example.aiend.vo;

import lombok.Data;

import java.util.List;

/**
 * 月度排行榜视图对象
 * 包含期数、排行列表及奖励信息
 *
 * @author AI-End
 * @since 2026-02-27
 */
@Data
public class MonthlyRankingVO {
    
    /**
     * 期数 (e.g., "2026-03")
     */
    private String period;
    
    /**
     * 排行列表
     */
    private List<RankingItemVO> list;
    
    /**
     * 奖励信息（仅已发放月份有）
     */
    private List<RewardInfoItem> rewardInfo;
    
    /**
     * 奖励信息项
     */
    @Data
    public static class RewardInfoItem {
        /**
         * 排名
         */
        private Integer rank;
        
        /**
         * 奖励金额
         */
        private Integer rewardAmount;
    }
}
