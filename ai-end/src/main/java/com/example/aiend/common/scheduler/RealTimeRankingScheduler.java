package com.example.aiend.common.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aiend.common.enums.TaskStatusEnum;
import com.example.aiend.entity.Task;
import com.example.aiend.entity.User;
import com.example.aiend.mapper.TaskMapper;
import com.example.aiend.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 实时排行榜定时任务
 * 每5分钟统计当月接单数前5名志愿者，存入Redis供用户端查询
 *
 * @author AI-End
 * @since 2026-02-27
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RealTimeRankingScheduler {

    private final TaskMapper taskMapper;
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Redis实时排行榜缓存Key
     */
    private static final String REALTIME_RANKING_KEY = "ranking:realtime:top5";

    /**
     * 排名前几名
     */
    private static final int TOP_RANK_COUNT = 5;

    /**
     * 每5分钟执行实时排行榜统计
     * cron表达式: 秒 分 时 日 月 周
     * 0 0/5 * * * ? 表示每5分钟执行一次
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void executeRealTimeRankingStatistics() {
        log.info("开始执行实时排行榜统计任务...");

        try {
            // 计算当月的时间范围
            YearMonth currentMonth = YearMonth.now();
            String period = currentMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            LocalDateTime startTime = currentMonth.atDay(1).atStartOfDay();
            LocalDateTime endTime = LocalDateTime.now();

            log.info("统计当月排行榜，期数: {}，时间范围: {} 至 {}", period, startTime, endTime);

            // 1. 统计当月完成任务的志愿者接单数
            List<VolunteerRankingData> volunteerRankings = statisticsCurrentMonthOrders(startTime, endTime);

            // 2. 取前5名，组装排行榜数据
            List<RankingItemData> rankingList = generateRankingList(period, volunteerRankings);

            // 3. 存入Redis
            saveToRedis(period, rankingList);

            log.info("实时排行榜统计完成，期数: {}，统计人数: {}", period, rankingList.size());

        } catch (Exception e) {
            log.error("实时排行榜统计任务执行失败", e);
        }
    }

    /**
     * 统计当月志愿者接单数
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 志愿者接单数列表（按接单数降序排列）
     */
    private List<VolunteerRankingData> statisticsCurrentMonthOrders(LocalDateTime startTime, LocalDateTime endTime) {
        // 查询已完成的任务（status=3），在当月时间范围内
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getStatus, TaskStatusEnum.COMPLETED.getCode())
                .isNotNull(Task::getVolunteerId)
                .ge(Task::getUpdateTime, startTime)
                .le(Task::getUpdateTime, endTime);

        List<Task> completedTasks = taskMapper.selectList(wrapper);

        log.debug("当月已完成任务数: {}", completedTasks.size());

        // 按志愿者ID分组统计接单数
        Map<Long, Integer> volunteerOrderCountMap = new HashMap<>();
        for (Task task : completedTasks) {
            Long volunteerId = task.getVolunteerId();
            volunteerOrderCountMap.merge(volunteerId, 1, Integer::sum);
        }

        // 转换为列表并按接单数降序排列
        return volunteerOrderCountMap.entrySet().stream()
                .map(entry -> new VolunteerRankingData(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> b.orderCount - a.orderCount)
                .collect(Collectors.toList());
    }

    /**
     * 生成排行榜数据列表
     *
     * @param period           期数
     * @param volunteerRankings 志愿者排名列表
     * @return 排行榜数据列表
     */
    private List<RankingItemData> generateRankingList(String period, List<VolunteerRankingData> volunteerRankings) {
        List<RankingItemData> rankingList = new ArrayList<>();

        // 取前5名（或实际人数）
        int actualCount = Math.min(TOP_RANK_COUNT, volunteerRankings.size());

        for (int i = 0; i < actualCount; i++) {
            VolunteerRankingData volunteerData = volunteerRankings.get(i);
            int rank = i + 1;

            // 查询志愿者信息
            User volunteer = userMapper.selectById(volunteerData.volunteerId);
            if (volunteer == null) {
                log.warn("志愿者不存在，ID: {}", volunteerData.volunteerId);
                continue;
            }

            RankingItemData item = new RankingItemData();
            item.setRank(rank);
            item.setVolunteerId(volunteerData.volunteerId.toString());
            item.setVolunteerName(volunteer.getRealName() != null ? volunteer.getRealName() : volunteer.getNickname());
            item.setVolunteerAvatar(volunteer.getAvatar());
            item.setOrderCount(volunteerData.orderCount);

            rankingList.add(item);
        }

        return rankingList;
    }

    /**
     * 保存排行榜数据到Redis
     *
     * @param period      期数
     * @param rankingList 排行榜数据列表
     */
    private void saveToRedis(String period, List<RankingItemData> rankingList) {
        // 创建包含期数和列表的数据结构
        Map<String, Object> rankingData = new HashMap<>();
        rankingData.put("period", period);
        rankingData.put("list", rankingList);
        rankingData.put("updateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // 存入Redis，使用Hash结构
        redisTemplate.opsForValue().set(REALTIME_RANKING_KEY, rankingData);
        // 设置过期时间为10分钟（比刷新间隔长一点，防止数据丢失）
        redisTemplate.expire(REALTIME_RANKING_KEY, 10, TimeUnit.MINUTES);

        log.info("实时排行榜数据已存入Redis，Key: {}", REALTIME_RANKING_KEY);
    }

    /**
     * 手动触发实时排行榜统计（供测试使用）
     */
    public void manualExecute() {
        log.info("手动触发实时排行榜统计");
        executeRealTimeRankingStatistics();
    }

    /**
     * 志愿者接单数内部类
     */
    private static class VolunteerRankingData {
        Long volunteerId;
        int orderCount;

        VolunteerRankingData(Long volunteerId, int orderCount) {
            this.volunteerId = volunteerId;
            this.orderCount = orderCount;
        }
    }

    /**
     * 排行榜项数据类
     * 用于Redis序列化存储
     */
    @lombok.Data
    public static class RankingItemData implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        
        private Integer rank;
        private String volunteerId;
        private String volunteerName;
        private String volunteerAvatar;
        private Integer orderCount;
    }
}
