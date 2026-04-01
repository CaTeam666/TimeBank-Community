package com.example.aiend.service.client;

import com.example.aiend.vo.MonthlyRankingVO;
import com.example.aiend.vo.UserOrderStatsVO;

/**
 * 用户端排行榜服务接口
 *
 * @author AI-End
 * @since 2026-02-27
 */
public interface ClientRankingService {
    
    /**
     * 获取当月前五名排行榜
     * 从Redis缓存中获取实时排行数据
     *
     * @return 月度排行榜数据
     */
    MonthlyRankingVO getCurrentTopRanking();
    
    /**
     * 根据月份获取历史排行榜
     * 优先从Redis缓存获取，没有则查询数据库
     *
     * @param period 期数 (格式: YYYY-MM)
     * @return 月度排行榜数据
     */
    MonthlyRankingVO getMonthlyRanking(String period);
    
    /**
     * 获取当前用户的接单统计
     *
     * @param userId 用户ID
     * @return 用户接单统计数据
     */
    UserOrderStatsVO getUserOrderStats(Long userId);
}
