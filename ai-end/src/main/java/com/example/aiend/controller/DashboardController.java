package com.example.aiend.controller;

import com.example.aiend.common.Result;
import com.example.aiend.service.DashboardService;
import com.example.aiend.vo.DailyCoinStatsVO;
import com.example.aiend.vo.dashboard.DynamicVO;
import com.example.aiend.vo.dashboard.KpiVO;
import com.example.aiend.vo.dashboard.TaskTypeDistributionVO;
import com.example.aiend.vo.dashboard.TrendVO;
import com.example.aiend.vo.dashboard.VolunteerRankingVO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 数据驾驶舱控制器 (系统端)
 *
 * @author AI-End
 * @since 2026-03-22
 */
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 获取今日实时统计数据
     *
     * @return 今日统计数据
     */
    @GetMapping("/today")
    public Result<Map<String, Object>> getTodayStatistics() {
        return Result.success(dashboardService.getTodayStatistics());
    }

    /**
     * 获取历史日统计数据（按日期范围）
     *
     * @param startDate 开始日期，格式：yyyy-MM-dd
     * @param endDate   结束日期，格式：yyyy-MM-dd
     * @return 历史数据列表
     */
    @GetMapping("/history")
    public Result<List<DailyCoinStatsVO>> getHistoryStatistics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(dashboardService.getHistoryStatistics(startDate, endDate));
    }

    /**
     * 获取系统总览数据（用户总数、任务总数、总流通量等概览）
     *
     * @return 总览数据
     */
    @GetMapping("/overview")
    public Result<Map<String, Object>> getSystemOverview() {
        return Result.success(dashboardService.getSystemOverview());
    }

    /**
     * 获取核心指标统计 (KPI卡片)
     */
    @GetMapping("/kpi")
    public Result<KpiVO> getDashboardKpi() {
        return Result.success(dashboardService.getDashboardKpi());
    }

    /**
     * 获取近7日服务活跃度趋势
     */
    @GetMapping("/trend/activity")
    public Result<List<TrendVO>> getActivityTrend() {
        return Result.success(dashboardService.getActivityTrend());
    }

    /**
     * 获取任务类型分布
     */
    @GetMapping("/distribution/task-type")
    public Result<List<TaskTypeDistributionVO>> getTaskTypeDistribution() {
        return Result.success(dashboardService.getTaskTypeDistribution());
    }

    /**
     * 获取实时动态
     *
     * @param limit 返回条数，默认 20
     */
    @GetMapping("/dynamics")
    public Result<List<DynamicVO>> getDynamics(@RequestParam(required = false, defaultValue = "20") Integer limit) {
        return Result.success(dashboardService.getDynamics(limit));
    }

    /**
     * 获取本月志愿者荣誉榜 Top 5
     */
    @GetMapping("/ranking/volunteers")
    public Result<List<VolunteerRankingVO>> getVolunteerRanking() {
        return Result.success(dashboardService.getVolunteerRanking());
    }
}
