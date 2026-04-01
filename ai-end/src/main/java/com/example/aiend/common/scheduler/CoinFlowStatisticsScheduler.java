package com.example.aiend.common.scheduler;

import com.example.aiend.entity.DailyCoinStats;
import com.example.aiend.mapper.CoinLogMapper;
import com.example.aiend.mapper.DailyCoinStatsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 资金流通统计定时任务
 * 每5分钟统计资金流通数量，缓存到Redis
 *
 * @author AI-End
 * @since 2026-01-02
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CoinFlowStatisticsScheduler {

    private final CoinLogMapper coinLogMapper;
    private final DailyCoinStatsMapper dailyCoinStatsMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Redis缓存Key：资金流通统计
     */
    private static final String COIN_FLOW_STATS_KEY = "coin:flow:statistics";

    /**
     * 缓存过期时间（分钟）
     */
    private static final long CACHE_EXPIRE_MINUTES = 10;

    /**
     * 每5分钟执行一次资金流通统计
     * cron表达式：0 0/5 * * * ? 表示每5分钟执行一次
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void statisticsCoinFlow() {
        log.info("开始执行资金流通统计定时任务...");
        
        try {
            // 统计各类型资金流通数量
            Map<String, Object> statistics = new HashMap<>();
            
            // 1. 统计今日总收入（任务收入，type=1）
            Integer todayIncome = coinLogMapper.sumTodayAmountByType(1);
            statistics.put("todayIncome", todayIncome != null ? todayIncome : 0);
            
            // 2. 统计今日总支出（任务支出，type=2）
            Integer todayExpense = coinLogMapper.sumTodayAmountByType(2);
            statistics.put("todayExpense", todayExpense != null ? Math.abs(todayExpense) : 0);
            
            // 3. 统计今日兑换支出（type=3）
            Integer todayExchange = coinLogMapper.sumTodayAmountByType(3);
            statistics.put("todayExchange", todayExchange != null ? Math.abs(todayExchange) : 0);
            
            // 4. 统计今日交易笔数
            Integer todayTransactionCount = coinLogMapper.countTodayTransactions();
            statistics.put("todayTransactionCount", todayTransactionCount != null ? todayTransactionCount : 0);
            
            // 5. 统计总流通量（所有收入总和）
            Integer totalCirculation = coinLogMapper.sumAllIncome();
            statistics.put("totalCirculation", totalCirculation != null ? totalCirculation : 0);
            
            // 6. 记录统计时间
            statistics.put("statisticsTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            // 缓存到Redis
            redisTemplate.opsForValue().set(COIN_FLOW_STATS_KEY, statistics, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            
            log.info("资金流通统计完成，今日收入：{}，今日支出：{}，今日交易笔数：{}，总流通量：{}",
                    statistics.get("todayIncome"),
                    statistics.get("todayExpense"),
                    statistics.get("todayTransactionCount"),
                    statistics.get("totalCirculation"));
            
        } catch (Exception e) {
            log.error("资金流通统计失败", e);
        }
    }

    /**
     * 获取资金流通统计（供其他服务调用）
     *
     * @return 统计数据
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getCoinFlowStatistics() {
        Object data = redisTemplate.opsForValue().get(COIN_FLOW_STATS_KEY);
        if (data instanceof Map) {
            return (Map<String, Object>) data;
        }
        // 缓存不存在，立即执行统计
        statisticsCoinFlow();
        return (Map<String, Object>) redisTemplate.opsForValue().get(COIN_FLOW_STATS_KEY);
    }
}
