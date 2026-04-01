package com.example.aiend.service.client.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aiend.common.enums.TaskStatusEnum;
import com.example.aiend.common.exception.BusinessException;
import com.example.aiend.common.scheduler.RealTimeRankingScheduler;
import com.example.aiend.entity.RankingLog;
import com.example.aiend.entity.Task;
import com.example.aiend.mapper.RankingLogMapper;
import com.example.aiend.mapper.TaskMapper;
import com.example.aiend.service.client.ClientRankingService;
import com.example.aiend.vo.MonthlyRankingVO;
import com.example.aiend.vo.RankingItemVO;
import com.example.aiend.vo.UserOrderStatsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户端排行榜服务实现类
 *
 * @author AI-End
 * @since 2026-02-27
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ClientRankingServiceImpl implements ClientRankingService {

    private final TaskMapper taskMapper;
    private final RankingLogMapper rankingLogMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RealTimeRankingScheduler realTimeRankingScheduler;

    /**
     * Redis实时排行榜缓存Key
     */
    private static final String REALTIME_RANKING_KEY = "ranking:realtime:top5";

    /**
     * Redis月度排名缓存Key前缀
     */
    private static final String RANKING_CACHE_KEY_PREFIX = "ranking:monthly:";

    /**
     * 奖励时间币配置（第1名到第5名）
     */
    private static final int[] REWARD_AMOUNTS = {1000, 500, 300, 300, 300};

    /**
     * 获取当月前五名排行榜
     * 从Redis缓存中获取实时排行数据
     */
    @Override
    public MonthlyRankingVO getCurrentTopRanking() {
        log.info("获取当月前五名排行榜");

        // 从Redis获取实时排行数据
        Object cachedData = redisTemplate.opsForValue().get(REALTIME_RANKING_KEY);

        MonthlyRankingVO result = new MonthlyRankingVO();

        if (cachedData != null) {
            try {
                // 解析Redis中的数据
                if (cachedData instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> dataMap = (Map<String, Object>) cachedData;
                    result.setPeriod((String) dataMap.get("period"));

                    // 解析排行列表
                    Object listObj = dataMap.get("list");
                    if (listObj instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<Object> rawList = (List<Object>) listObj;
                        List<RankingItemVO> rankingList = new ArrayList<>();
                        
                        for (Object item : rawList) {
                            RankingItemVO vo = convertToRankingItemVO(item);
                            if (vo != null) {
                                rankingList.add(vo);
                            }
                        }
                        result.setList(rankingList);
                    }
                }
                log.info("从Redis缓存获取实时排行榜成功，期数: {}", result.getPeriod());
                return result;
            } catch (Exception e) {
                log.warn("解析Redis缓存数据失败: {}", e.getMessage());
            }
        }

        // Redis没有数据，手动触发一次统计
        log.info("Redis中没有实时排行数据，触发一次统计");
        realTimeRankingScheduler.manualExecute();

        // 再次尝试获取
        cachedData = redisTemplate.opsForValue().get(REALTIME_RANKING_KEY);
        if (cachedData instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> dataMap = (Map<String, Object>) cachedData;
            result.setPeriod((String) dataMap.get("period"));
            
            Object listObj = dataMap.get("list");
            if (listObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> rawList = (List<Object>) listObj;
                List<RankingItemVO> rankingList = new ArrayList<>();
                
                for (Object item : rawList) {
                    RankingItemVO vo = convertToRankingItemVO(item);
                    if (vo != null) {
                        rankingList.add(vo);
                    }
                }
                result.setList(rankingList);
            }
        } else {
            // 如果还是没有数据，返回空列表
            result.setPeriod(YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM")));
            result.setList(new ArrayList<>());
        }

        return result;
    }

    /**
     * 根据月份获取历史排行榜
     * 优先从Redis缓存获取，没有则查询数据库
     */
    @Override
    public MonthlyRankingVO getMonthlyRanking(String period) {
        Objects.requireNonNull(period, "期数不能为空");

        // 验证期数格式
        if (!period.matches("\\d{4}-\\d{2}")) {
            throw new BusinessException(400, "期数格式错误，正确格式: YYYY-MM");
        }

        log.info("获取月度排行榜，期数: {}", period);

        MonthlyRankingVO result = new MonthlyRankingVO();
        result.setPeriod(period);

        // 1. 优先从Redis缓存获取（月度历史数据）
        List<RankingItemVO> cachedList = getFromRedisCache(period);
        if (cachedList != null && !cachedList.isEmpty()) {
            log.info("从Redis缓存获取历史排行榜成功，期数: {}", period);
            result.setList(cachedList);
            result.setRewardInfo(buildRewardInfo());
            return result;
        }

        // 2. Redis没有，查询数据库
        List<RankingLog> rankingLogs = getFromDatabase(period);
        if (rankingLogs == null || rankingLogs.isEmpty()) {
            throw new BusinessException(404, "该月份暂无排行数据");
        }

        // 转换为VO
        List<RankingItemVO> voList = rankingLogs.stream()
                .map(this::convertToRankingItemVO)
                .collect(Collectors.toList());

        result.setList(voList);
        result.setRewardInfo(buildRewardInfo());

        return result;
    }

    /**
     * 获取当前用户的接单统计
     */
    @Override
    public UserOrderStatsVO getUserOrderStats(Long userId) {
        Objects.requireNonNull(userId, "用户ID不能为空");

        log.info("获取用户接单统计，用户ID: {}", userId);

        UserOrderStatsVO result = new UserOrderStatsVO();
        result.setUserId(userId.toString());

        // 1. 统计总接单数（所有已完成的任务）
        LambdaQueryWrapper<Task> totalWrapper = new LambdaQueryWrapper<>();
        totalWrapper.eq(Task::getVolunteerId, userId)
                .eq(Task::getStatus, TaskStatusEnum.COMPLETED.getCode());
        Long totalCount = taskMapper.selectCount(totalWrapper);
        result.setTotalOrderCount(totalCount.intValue());

        // 2. 统计当月接单数
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startTime = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endTime = LocalDateTime.now();

        LambdaQueryWrapper<Task> monthWrapper = new LambdaQueryWrapper<>();
        monthWrapper.eq(Task::getVolunteerId, userId)
                .eq(Task::getStatus, TaskStatusEnum.COMPLETED.getCode())
                .ge(Task::getUpdateTime, startTime)
                .le(Task::getUpdateTime, endTime);
        Long monthCount = taskMapper.selectCount(monthWrapper);
        result.setCurrentMonthCount(monthCount.intValue());

        // 3. 计算当前排名
        Integer currentRank = calculateCurrentRank(userId, startTime, endTime);
        result.setCurrentRank(currentRank);

        log.info("用户接单统计完成，用户ID: {}，总接单: {}，当月接单: {}，当前排名: {}",
                userId, result.getTotalOrderCount(), result.getCurrentMonthCount(), result.getCurrentRank());

        return result;
    }

    /**
     * 从Redis缓存获取月度排行数据
     */
    private List<RankingItemVO> getFromRedisCache(String period) {
        String cacheKey = RANKING_CACHE_KEY_PREFIX + period;

        try {
            List<Object> cachedData = redisTemplate.opsForList().range(cacheKey, 0, -1);
            if (cachedData == null || cachedData.isEmpty()) {
                return null;
            }

            return cachedData.stream()
                    .filter(obj -> obj instanceof RankingLog)
                    .map(obj -> convertToRankingItemVO((RankingLog) obj))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("从Redis获取月度排名数据失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从数据库获取月度排行数据
     */
    private List<RankingLog> getFromDatabase(String period) {
        LambdaQueryWrapper<RankingLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RankingLog::getPeriod, period)
                .orderByAsc(RankingLog::getRank);
        return rankingLogMapper.selectList(wrapper);
    }

    /**
     * 计算用户当前排名
     */
    private Integer calculateCurrentRank(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        // 统计当月所有志愿者的接单数
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getStatus, TaskStatusEnum.COMPLETED.getCode())
                .isNotNull(Task::getVolunteerId)
                .ge(Task::getUpdateTime, startTime)
                .le(Task::getUpdateTime, endTime);

        List<Task> completedTasks = taskMapper.selectList(wrapper);

        // 按志愿者分组统计
        Map<Long, Integer> volunteerOrderCountMap = new HashMap<>();
        for (Task task : completedTasks) {
            Long volunteerId = task.getVolunteerId();
            volunteerOrderCountMap.merge(volunteerId, 1, Integer::sum);
        }

        // 按接单数降序排序
        List<Map.Entry<Long, Integer>> sortedList = volunteerOrderCountMap.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .collect(Collectors.toList());

        // 查找用户排名
        for (int i = 0; i < sortedList.size() && i < 100; i++) {
            if (sortedList.get(i).getKey().equals(userId)) {
                return i + 1;
            }
        }

        // 不在前100名
        return null;
    }

    /**
     * 构建奖励信息
     */
    private List<MonthlyRankingVO.RewardInfoItem> buildRewardInfo() {
        List<MonthlyRankingVO.RewardInfoItem> rewardInfo = new ArrayList<>();
        for (int i = 0; i < REWARD_AMOUNTS.length; i++) {
            MonthlyRankingVO.RewardInfoItem item = new MonthlyRankingVO.RewardInfoItem();
            item.setRank(i + 1);
            item.setRewardAmount(REWARD_AMOUNTS[i]);
            rewardInfo.add(item);
        }
        return rewardInfo;
    }

    /**
     * 将Object转换为RankingItemVO（处理Redis反序列化的数据）
     */
    private RankingItemVO convertToRankingItemVO(Object item) {
        if (item == null) {
            return null;
        }

        RankingItemVO vo = new RankingItemVO();

        if (item instanceof RealTimeRankingScheduler.RankingItemData) {
            RealTimeRankingScheduler.RankingItemData data = (RealTimeRankingScheduler.RankingItemData) item;
            vo.setRank(data.getRank());
            vo.setVolunteerId(data.getVolunteerId());
            vo.setVolunteerName(data.getVolunteerName());
            vo.setVolunteerAvatar(data.getVolunteerAvatar());
            vo.setOrderCount(data.getOrderCount());
        } else if (item instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) item;
            vo.setRank(getIntValue(map.get("rank")));
            vo.setVolunteerId(String.valueOf(map.get("volunteerId")));
            vo.setVolunteerName((String) map.get("volunteerName"));
            vo.setVolunteerAvatar((String) map.get("volunteerAvatar"));
            vo.setOrderCount(getIntValue(map.get("orderCount")));
        } else if (item instanceof LinkedHashMap) {
            @SuppressWarnings("unchecked")
            LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) item;
            vo.setRank(getIntValue(map.get("rank")));
            vo.setVolunteerId(String.valueOf(map.get("volunteerId")));
            vo.setVolunteerName((String) map.get("volunteerName"));
            vo.setVolunteerAvatar((String) map.get("volunteerAvatar"));
            vo.setOrderCount(getIntValue(map.get("orderCount")));
        }

        return vo;
    }

    /**
     * 将RankingLog转换为RankingItemVO
     */
    private RankingItemVO convertToRankingItemVO(RankingLog rankingLog) {
        RankingItemVO vo = new RankingItemVO();
        vo.setRank(rankingLog.getRank());
        vo.setVolunteerId(rankingLog.getVolunteerId().toString());
        vo.setVolunteerName(rankingLog.getVolunteerName());
        vo.setVolunteerAvatar(rankingLog.getVolunteerAvatar());
        vo.setOrderCount(rankingLog.getOrderCount());
        return vo;
    }

    /**
     * 安全获取Integer值
     */
    private Integer getIntValue(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Integer) {
            return (Integer) obj;
        }
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        try {
            return Integer.parseInt(obj.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
