package com.example.aiend.service;

import com.example.aiend.vo.DailyCoinStatsVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 数据驾驶舱业务接口
 *
 * @author AI-End
 * @since 2026-03-22
 */
public interface DashboardService {

    /**
     * 获取今日实时统计数据
     *
     * @return 今日统计
     */
    Map<String, Object> getTodayStatistics();

    /**
     * 获取历史日统计数据（按日期范围）
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 历史数据列表
     */
    List<DailyCoinStatsVO> getHistoryStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * 获取系统总览数据（用户总数、任务总数、总流通量）
     *
     * @return 总览数据
     */
    Map<String, Object> getSystemOverview();

    /**
     * 获取数据驾驶舱核心指标统计 (KPI卡片)
     */
    com.example.aiend.vo.dashboard.KpiVO getDashboardKpi();

    /**
     * 获取近7日服务活跃度趋势
     */
    java.util.List<com.example.aiend.vo.dashboard.TrendVO> getActivityTrend();

    /**
     * 获取任务类型分布
     */
    java.util.List<com.example.aiend.vo.dashboard.TaskTypeDistributionVO> getTaskTypeDistribution();

    /**
     * 获取实时动态
     * @param limit 条数
     */
    java.util.List<com.example.aiend.vo.dashboard.DynamicVO> getDynamics(Integer limit);

    /**
     * 获取本月志愿者荣誉榜 Top 5
     */
    java.util.List<com.example.aiend.vo.dashboard.VolunteerRankingVO> getVolunteerRanking();
}
