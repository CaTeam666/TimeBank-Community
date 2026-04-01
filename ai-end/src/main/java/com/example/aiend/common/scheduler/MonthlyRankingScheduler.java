package com.example.aiend.common.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aiend.common.enums.TaskStatusEnum;
import com.example.aiend.dto.request.SystemSettingsDTO;
import com.example.aiend.entity.CoinLog;
import com.example.aiend.entity.RankingLog;
import com.example.aiend.entity.Task;
import com.example.aiend.entity.User;
import com.example.aiend.mapper.CoinLogMapper;
import com.example.aiend.mapper.RankingLogMapper;
import com.example.aiend.mapper.TaskMapper;
import com.example.aiend.mapper.UserMapper;
import com.example.aiend.service.SettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
 * 月度排名统计调度器
 * 每月初1日自动统计上月接单数前5名的志愿者，存入tb_ranking_log并同步到Redis
 *
 * @author AI-End
 * @since 2026-03-11
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class MonthlyRankingScheduler {

    private final TaskMapper taskMapper;
    private final UserMapper userMapper;
    private final RankingLogMapper rankingLogMapper;
    private final CoinLogMapper coinLogMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SettingsService settingsService;

    /**
     * Redis排名缓存Key前缀
     */
    private static final String RANKING_CACHE_KEY_PREFIX = "ranking:monthly:";

    /**
     * 时间币流水类型 - 排名奖励
     */
    private static final int COIN_LOG_TYPE_RANKING_REWARD = 5;

    /**
     * 排名前几名
     */
    private static final int TOP_RANK_COUNT = 5;

    /**
     * 从数据库动态读取各名次的奖励时间币配置
     *
     * @return 第1名到第5名的奖励金额数组
     */
    private BigDecimal[] getRewardAmounts() {
        SystemSettingsDTO settings = settingsService.getSettings();
        return new BigDecimal[] {
                new BigDecimal(settings.getMonthlyRank1Reward()), // 第1名
                new BigDecimal(settings.getMonthlyRank2Reward()), // 第2名
                new BigDecimal(settings.getMonthlyRank3Reward()), // 第3名
                new BigDecimal(settings.getMonthlyRank4Reward()), // 第4名
                new BigDecimal(settings.getMonthlyRank5Reward())  // 第5名
        };
    }

    /**
     * 每月1日凌晨1点执行月度排名统计
     * cron表达式: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 1 1 * ?")
    @Transactional(rollbackFor = Exception.class)
    public void executeMonthlyRankingStatistics() {
        log.info("开始执行月度排名统计任务...");

        try {
            // 计算上个月的期数 (格式: YYYY-MM)
            YearMonth lastMonth = YearMonth.now().minusMonths(1);
            String period = lastMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));

            // 检查是否已经统计过该期数
            if (isAlreadyStatisticsForPeriod(period)) {
                log.info("期数 {} 已存在统计数据，跳过本次统计", period);
                return;
            }

            // 计算上个月的时间范围
            LocalDateTime startTime = lastMonth.atDay(1).atStartOfDay();
            LocalDateTime endTime = lastMonth.atEndOfMonth().atTime(23, 59, 59);

            log.info("统计期数: {}，时间范围: {} 至 {}", period, startTime, endTime);

            // 1. 统计上个月完成任务的志愿者接单数
            List<VolunteerOrderCount> volunteerRankings = statisticsVolunteerOrders(startTime, endTime);

            if (volunteerRankings.isEmpty()) {
                log.info("期数 {} 没有完成的订单数据，跳过统计", period);
                return;
            }

            // 2. 动态获取奖金配置，取前5名，生成排名日志
            BigDecimal[] rewardAmounts = getRewardAmounts();
            List<RankingLog> rankingLogs = generateRankingLogs(period, volunteerRankings, rewardAmounts);

            // 3. 批量保存到数据库（自动任务始终发放时间币）
            saveRankingLogs(rankingLogs, true);

            // 4. 同步到Redis
            syncToRedis(period, rankingLogs);

            log.info("月度排名统计完成，期数: {}，统计人数: {}", period, rankingLogs.size());

        } catch (Exception e) {
            log.error("月度排名统计任务执行失败", e);
            throw e;
        }
    }

    /**
     * 检查指定期数是否已存在统计数据
     *
     * @param period 期数
     * @return 是否已存在
     */
    private boolean isAlreadyStatisticsForPeriod(String period) {
        LambdaQueryWrapper<RankingLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RankingLog::getPeriod, period);
        return rankingLogMapper.selectCount(wrapper) > 0;
    }

    /**
     * 检查指定期数的数据是否已锁定（已有5条数据，不允许修改）
     * 当某个月份已有完整的5条排名数据时，该月份数据被锁定，不允许任何操作修改
     *
     * @param period 期数
     * @return 是否已锁定
     */
    private boolean isDataLocked(String period) {
        LambdaQueryWrapper<RankingLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RankingLog::getPeriod, period);
        Long count = rankingLogMapper.selectCount(wrapper);
        return count >= TOP_RANK_COUNT;
    }

    /**
     * 统计志愿者接单数
     * 查询指定时间范围内完成的任务，按志愿者分组统计接单数
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 志愿者接单数列表（按接单数降序排列）
     */
    private List<VolunteerOrderCount> statisticsVolunteerOrders(LocalDateTime startTime, LocalDateTime endTime) {
        // 查询已完成的任务（status=3），在指定时间范围内
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getStatus, TaskStatusEnum.COMPLETED.getCode())
                .isNotNull(Task::getVolunteerId)
                .ge(Task::getUpdateTime, startTime)
                .le(Task::getUpdateTime, endTime);

        List<Task> completedTasks = taskMapper.selectList(wrapper);

        log.info("查询到 {} 条已完成任务", completedTasks.size());

        // 按志愿者ID分组统计接单数
        Map<Long, Integer> volunteerOrderCountMap = new HashMap<>();
        for (Task task : completedTasks) {
            Long volunteerId = task.getVolunteerId();
            volunteerOrderCountMap.merge(volunteerId, 1, Integer::sum);
        }

        // 转换为列表并按接单数降序排列
        List<VolunteerOrderCount> result = volunteerOrderCountMap.entrySet().stream()
                .map(entry -> new VolunteerOrderCount(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> b.orderCount - a.orderCount)
                .collect(Collectors.toList());

        log.info("统计到 {} 位志愿者有完成订单", result.size());

        return result;
    }

    /**
     * 生成排名日志记录
     *
     * @param period           期数
     * @param volunteerRankings 志愿者排名列表
     * @return 排名日志列表
     */
    private List<RankingLog> generateRankingLogs(String period, List<VolunteerOrderCount> volunteerRankings, BigDecimal[] rewardAmounts) {
        List<RankingLog> rankingLogs = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // 取前5名（或实际人数）
        int actualCount = Math.min(TOP_RANK_COUNT, volunteerRankings.size());

        for (int i = 0; i < actualCount; i++) {
            VolunteerOrderCount volunteerData = volunteerRankings.get(i);
            int rank = i + 1;

            // 查询志愿者信息
            User volunteer = userMapper.selectById(volunteerData.volunteerId);
            if (volunteer == null) {
                log.warn("志愿者不存在，ID: {}", volunteerData.volunteerId);
                continue;
            }

            RankingLog rankingLog = new RankingLog();
            rankingLog.setPeriod(period);
            rankingLog.setRank(rank);
            rankingLog.setVolunteerId(volunteerData.volunteerId);
            rankingLog.setVolunteerName(volunteer.getRealName() != null ? volunteer.getRealName() : volunteer.getNickname());
            rankingLog.setVolunteerAvatar(volunteer.getAvatar());
            rankingLog.setOrderCount(volunteerData.orderCount);
            rankingLog.setRewardAmount(rewardAmounts[i]);
            rankingLog.setDistributionTime(now);
            rankingLog.setStatus("SUCCESS");
            rankingLog.setCreateTime(now);
            rankingLog.setUpdateTime(now);
            rankingLog.setIsDeleted(0);

            rankingLogs.add(rankingLog);

            log.info("排名第{}名: 志愿者ID={}, 姓名={}, 接单数={}, 奖励={}",
                    rank, volunteerData.volunteerId, rankingLog.getVolunteerName(),
                    volunteerData.orderCount, rewardAmounts[i]);
        }

        return rankingLogs;
    }

    /**
     * 批量保存排名日志到数据库
     *
     * @param rankingLogs 排名日志列表
     * @param distributeReward 是否发放时间币奖励
     */
    private void saveRankingLogs(List<RankingLog> rankingLogs, boolean distributeReward) {
        LocalDateTime now = LocalDateTime.now();
        
        for (RankingLog rankingLog : rankingLogs) {
            // 1. 保存排名日志
            rankingLogMapper.insert(rankingLog);
            
            // 2. 根据参数决定是否发放时间币
            if (distributeReward) {
                int rewardAmount = rankingLog.getRewardAmount().intValue();
                User volunteer = userMapper.selectById(rankingLog.getVolunteerId());
                if (volunteer != null) {
                    // 更新用户余额
                    volunteer.setBalance(volunteer.getBalance() + rewardAmount);
                    volunteer.setUpdateTime(now);
                    userMapper.updateById(volunteer);
                    
                    // 记录时间币流水
                    CoinLog coinLog = new CoinLog();
                    coinLog.setUserId(rankingLog.getVolunteerId());
                    coinLog.setAmount(rewardAmount);
                    coinLog.setType(COIN_LOG_TYPE_RANKING_REWARD);
                    coinLog.setTaskId(null); // 排名奖励不关联任务
                    coinLog.setCreateTime(now);
                    coinLog.setUpdateTime(now);
                    coinLog.setIsDeleted(0);
                    coinLogMapper.insert(coinLog);
                    
                    log.info("已发放排名奖励: 志愿者ID={}, 姓名={}, 奖励={}时间币", 
                            rankingLog.getVolunteerId(), rankingLog.getVolunteerName(), rewardAmount);
                }
            }
        }
        
        if (distributeReward) {
            log.info("成功保存 {} 条排名日志到数据库，并已发放奖励到账户", rankingLogs.size());
        } else {
            log.info("成功保存 {} 条排名日志到数据库（未发放时间币，因Redis已存在缓存）", rankingLogs.size());
        }
    }

    /**
     * 同步排名数据到Redis
     *
     * @param period      期数
     * @param rankingLogs 排名日志列表
     */
    private void syncToRedis(String period, List<RankingLog> rankingLogs) {
        String cacheKey = RANKING_CACHE_KEY_PREFIX + period;

        // 将排名数据存入Redis List
        redisTemplate.delete(cacheKey);
        for (RankingLog rankingLog : rankingLogs) {
            redisTemplate.opsForList().rightPush(cacheKey, rankingLog);
        }

        // 设置过期时间为90天
        redisTemplate.expire(cacheKey, 90, TimeUnit.DAYS);

        log.info("成功同步 {} 条排名数据到Redis，Key: {}", rankingLogs.size(), cacheKey);
    }

    /**
     * 手动触发统计（可用于补跑）
     * 统计指定期数的排名数据
     * 注意：
     * 1. 如果数据库已有该月份5条数据，则数据被锁定，不允许任何修改
     * 2. 如果Redis中已存在该月份数据，则不再发放时间币（避免重复发放）
     *
     * @param period 期数 (格式: YYYY-MM)
     * @return 统计结果信息
     */
    public String manualExecuteStatistics(String period) {
        log.info("手动触发月度排名统计，期数: {}", period);

        try {
            // 检查数据库中该月份是否已有5条数据（锁定状态）
            if (isDataLocked(period)) {
                String msg = String.format("期数 %s 已有%d条排名数据，数据已锁定，不允许修改", period, TOP_RANK_COUNT);
                log.warn(msg);
                return msg;
            }

            // 检查Redis中是否已存在该月份数据
            String cacheKey = RANKING_CACHE_KEY_PREFIX + period;
            boolean existsInRedis = Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey)) 
                    && redisTemplate.opsForList().size(cacheKey) > 0;
            
            if (existsInRedis) {
                log.info("Redis中已存在期数 {} 的排名数据，本次补跑不再发放时间币", period);
            }

            // 解析期数
            YearMonth yearMonth = YearMonth.parse(period, DateTimeFormatter.ofPattern("yyyy-MM"));

            // 计算时间范围
            LocalDateTime startTime = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime endTime = yearMonth.atEndOfMonth().atTime(23, 59, 59);

            log.info("统计期数: {}，时间范围: {} 至 {}", period, startTime, endTime);

            // 删除已有数据库记录（只在未锁定时才会执行到这里）
            LambdaQueryWrapper<RankingLog> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(RankingLog::getPeriod, period);
            rankingLogMapper.delete(deleteWrapper);

            // 1. 统计志愿者接单数
            List<VolunteerOrderCount> volunteerRankings = statisticsVolunteerOrders(startTime, endTime);

            if (volunteerRankings.isEmpty()) {
                String msg = String.format("期数 %s 没有完成的订单数据", period);
                log.info(msg);
                return msg;
            }

            // 2. 动态获取奖金配置，生成排名日志
            BigDecimal[] rewardAmounts = getRewardAmounts();
            List<RankingLog> rankingLogs = generateRankingLogs(period, volunteerRankings, rewardAmounts);

            // 3. 保存到数据库（根据Redis是否存在决定是否发放时间币）
            saveRankingLogs(rankingLogs, !existsInRedis);

            // 4. 同步到Redis
            syncToRedis(period, rankingLogs);

            String msg = String.format("手动统计完成，期数: %s，统计人数: %d，是否发放时间币: %s", 
                    period, rankingLogs.size(), !existsInRedis);
            log.info(msg);
            return msg;

        } catch (Exception e) {
            log.error("手动统计失败，期数: {}", period, e);
            throw new RuntimeException("统计失败: " + e.getMessage());
        }
    }

    /**
     * 志愿者接单数内部类
     */
    private static class VolunteerOrderCount {
        Long volunteerId;
        int orderCount;

        VolunteerOrderCount(Long volunteerId, int orderCount) {
            this.volunteerId = volunteerId;
            this.orderCount = orderCount;
        }
    }
}
