package com.example.aiend.vo;

import lombok.Data;

/**
 * 用户接单统计视图对象
 * 包含用户的接单数据和当前排名
 *
 * @author AI-End
 * @since 2026-02-27
 */
@Data
public class UserOrderStatsVO {
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 总接单数（累计所有已完成任务）
     */
    private Integer totalOrderCount;
    
    /**
     * 当月接单数
     */
    private Integer currentMonthCount;
    
    /**
     * 当前排名（无排名则为null）
     * 如果不在前100名则返回null
     */
    private Integer currentRank;
}
