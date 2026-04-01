package com.example.aiend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aiend.common.scheduler.CoinFlowStatisticsScheduler;
import com.example.aiend.entity.DailyCoinStats;
import com.example.aiend.entity.Task;
import com.example.aiend.entity.User;
import com.example.aiend.mapper.CoinLogMapper;
import com.example.aiend.mapper.DailyCoinStatsMapper;
import com.example.aiend.mapper.DashboardMapper;
import com.example.aiend.mapper.ExchangeOrderMapper;
import com.example.aiend.mapper.TaskMapper;
import com.example.aiend.mapper.UserMapper;
import com.example.aiend.service.DashboardService;
import com.example.aiend.vo.DailyCoinStatsVO;
import com.example.aiend.vo.dashboard.DynamicVO;
import com.example.aiend.vo.dashboard.KpiVO;
import com.example.aiend.vo.dashboard.TaskTypeDistributionVO;
import com.example.aiend.vo.dashboard.TrendVO;
import com.example.aiend.vo.dashboard.VolunteerRankingVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据驾驶舱业务实现类
 *
 * @author AI-End
 * @since 2026-03-22
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final CoinFlowStatisticsScheduler coinFlowStatisticsScheduler;
    private final DailyCoinStatsMapper dailyCoinStatsMapper;
    private final CoinLogMapper coinLogMapper;
    private final UserMapper userMapper;
    private final TaskMapper taskMapper;
    private final ExchangeOrderMapper exchangeOrderMapper;
    private final DashboardMapper dashboardMapper;

    @Override
    public Map<String, Object> getTodayStatistics() {
        log.info("获取驾驶舱今日实时统计数据");
        return coinFlowStatisticsScheduler.getCoinFlowStatistics();
    }

    @Override
    public List<DailyCoinStatsVO> getHistoryStatistics(LocalDate startDate, LocalDate endDate) {
        log.info("获取驾驶舱历史统计数据，startDate: {}, endDate: {}", startDate, endDate);
        
        // 默认查询最近30天
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now().minusDays(1); // 默认不查今天，今天的数据调 today 接口
        }

        List<DailyCoinStats> statsList = dailyCoinStatsMapper.selectByDateRange(startDate, endDate);
        
        return statsList.stream().map(stats -> DailyCoinStatsVO.builder()
                .statDate(stats.getStatDate())
                .income(stats.getIncome())
                .expense(stats.getExpense())
                .exchange(stats.getExchange())
                .systemAdjust(stats.getSystemAdjust())
                .txCount(stats.getTxCount())
                .build()).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getSystemOverview() {
        log.info("获取系统总览数据");
        Map<String, Object> overview = new HashMap<>();

        // 1. 用户总数
        Long userCount = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getDeleted, 0));
        overview.put("totalUsers", userCount);

        // 2. 任务总数
        Long taskCount = taskMapper.selectCount(new LambdaQueryWrapper<Task>().eq(Task::getIsDeleted, 0));
        overview.put("totalTasks", taskCount);

        // 3. 总流通量（累计所有收入）
        Integer totalCirculation = coinLogMapper.sumAllIncome();
        overview.put("totalCirculation", totalCirculation != null ? totalCirculation : 0);

        return overview;
    }

    @Override
    public KpiVO getDashboardKpi() {
        log.info("获取驾驶舱核心指标统计");
        Long totalUsers = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getDeleted, 0));
        Integer todayExchange = exchangeOrderMapper.countTodayExchanges();
        Integer todayNewOrders = taskMapper.countTodayCompletedOrders();
        Integer todayCirculation = coinLogMapper.sumTodayCirculation();

        return KpiVO.builder()
                .totalPopulation(totalUsers != null ? totalUsers : 0L)
                .todayExchangeCount(todayExchange != null ? todayExchange : 0)
                .todayNewOrders(todayNewOrders != null ? todayNewOrders : 0)
                .todayPointsCirculation(todayCirculation != null ? todayCirculation : 0)
                .build();
    }

    @Override
    public List<TrendVO> getActivityTrend() {
        log.info("获取近7日服务活跃度趋势");
        return taskMapper.selectActivityTrend();
    }

    @Override
    public List<TaskTypeDistributionVO> getTaskTypeDistribution() {
        log.info("获取任务类型分布");
        return taskMapper.selectTaskTypeDistribution();
    }

    @Override
    public List<DynamicVO> getDynamics(Integer limit) {
        log.info("获取实时动态, limit={}", limit);
        if (limit == null || limit <= 0) {
            limit = 20;
        }
        return dashboardMapper.selectDynamics(limit);
    }

    @Override
    public List<VolunteerRankingVO> getVolunteerRanking() {
        log.info("获取本月志愿者荣誉榜 Top 5");
        List<VolunteerRankingVO> list = dashboardMapper.selectVolunteerRanking();
        // 设置排名
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                list.get(i).setRank(i + 1);
            }
        }
        return list;
    }
}
