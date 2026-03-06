package com.example.aiend.common.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aiend.common.enums.TaskStatusEnum;
import com.example.aiend.entity.Task;
import com.example.aiend.mapper.TaskMapper;
import com.example.aiend.service.impl.AnomalyServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 僵尸任务调度器
 * 定时检测并处理异常任务（服务开始时间前一小时还没有人接单的任务）
 *
 * @author AI-End
 * @since 2026-01-14
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ZombieTaskScheduler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final TaskMapper taskMapper;
    private final AnomalyServiceImpl anomalyService;
    private final ObjectMapper objectMapper;

    /**
     * Redis 任务分类 ZSet Key 前缀
     */
    private static final String TASK_CATEGORY_KEY_PREFIX = "task:category:";

    /**
     * 所有任务分类
     */
    private static final String[] TASK_CATEGORIES = {"跑腿代办", "生活照料", "医疗健康", "家政维修", "陪伴聊天", "其他", "陪聊", "保洁", "跑腿", "医疗陪护"};

    /**
     * 定时检测僵尸任务
     * 每30分钟执行一次，检查Redis和数据库中的异常任务
     */
    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void checkZombieTasks() {
        log.info("开始执行僵尸任务检测...");

        try {
            LocalDateTime now = LocalDateTime.now();
            // 服务开始时间前1小时阈值
            LocalDateTime warningThreshold = now.plusHours(1);
            
            // 1. 先检查Redis缓存中的任务（获取待接单任务ID列表，用于日志记录）
            checkRedisForZombieTasks(now, warningThreshold);

            // 2. 再检查数据库中的任务（确保完整性）
            List<Task> zombieTasks = checkDatabaseForZombieTasks(now, warningThreshold);

            // 3. 处理到达服务时间但未被接单的任务（自动取消）
            int cancelledCount = 0;
            for (Task task : zombieTasks) {
                // 检查是否已到达服务时间
                if (task.getServiceTime() != null && task.getServiceTime().isBefore(now) || task.getServiceTime().isEqual(now)) {
                    // 到达服务时间，自动取消
                    boolean success = anomalyService.processZombieTask(task);
                    if (success) {
                        cancelledCount++;
                    }
                }
            }

            // 4. 记录检测到但未取消的异常任务（预警）
            int warningCount = 0;
            for (Task task : zombieTasks) {
                if (task.getServiceTime() != null && task.getServiceTime().isAfter(now)) {
                    // 服务时间未到，但距离服务时间不足1小时
                    if (task.getServiceTime().isBefore(warningThreshold)) {
                        log.warn("预警：任务即将成为僵尸任务，任务ID：{}，标题：{}，服务时间：{}",
                                task.getId(), task.getTitle(), task.getServiceTime());
                        warningCount++;
                    }
                }
            }

            log.info("僵尸任务检测完成，预警任务数：{}，自动取消任务数：{}", warningCount, cancelledCount);

        } catch (Exception e) {
            log.error("僵尸任务检测失败", e);
        }
    }

    /**
     * 从Redis缓存中检测异常任务
     *
     * @param now              当前时间
     * @param warningThreshold 预警阈值时间
     * @return 异常任务ID列表
     */
    private List<Long> checkRedisForZombieTasks(LocalDateTime now, LocalDateTime warningThreshold) {
        List<Long> zombieTaskIds = new ArrayList<>();

        try {
            // 遍历所有分类的ZSet
            for (String category : TASK_CATEGORIES) {
                String key = TASK_CATEGORY_KEY_PREFIX + category;
                Set<Object> taskData = redisTemplate.opsForZSet().range(key, 0, -1);

                if (taskData != null && !taskData.isEmpty()) {
                    for (Object data : taskData) {
                        try {
                            // 解析任务数据
                            Map<String, Object> taskMap = objectMapper.convertValue(data, 
                                    new TypeReference<Map<String, Object>>() {});
                            
                            // 检查任务状态是否为待接单
                            Object statusObj = taskMap.get("status");
                            if (statusObj != null) {
                                int status = Integer.parseInt(statusObj.toString());
                                if (status != TaskStatusEnum.PENDING.getCode()) {
                                    continue;
                                }
                            }

                            // 获取任务ID
                            Object taskIdObj = taskMap.get("taskId");
                            if (taskIdObj != null) {
                                Long taskId = Long.parseLong(taskIdObj.toString());
                                zombieTaskIds.add(taskId);
                            }
                        } catch (Exception e) {
                            log.debug("解析Redis任务数据失败", e);
                        }
                    }
                }
            }

            log.info("从Redis缓存中检测到 {} 个待接单任务", zombieTaskIds.size());

        } catch (Exception e) {
            log.error("从Redis检测僵尸任务失败", e);
        }

        return zombieTaskIds;
    }

    /**
     * 从数据库中检测异常任务
     * 查找：状态为待接单、无志愿者接单、服务时间在当前时间到1小时后之间的任务
     *
     * @param now              当前时间
     * @param warningThreshold 预警阈值时间（服务时间前1小时）
     * @return 异常任务列表
     */
    private List<Task> checkDatabaseForZombieTasks(LocalDateTime now, LocalDateTime warningThreshold) {
        try {
            LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Task::getStatus, TaskStatusEnum.PENDING.getCode())  // 待接单状态
                    .isNull(Task::getVolunteerId)  // 无志愿者接单
                    .isNotNull(Task::getServiceTime)  // 有服务时间
                    .le(Task::getServiceTime, warningThreshold)  // 服务时间 <= 当前时间 + 1小时
                    .orderByAsc(Task::getServiceTime);

            List<Task> zombieTasks = taskMapper.selectList(queryWrapper);
            log.info("从数据库中检测到 {} 个异常任务（服务时间前1小时内未被接单）", zombieTasks.size());

            return zombieTasks;

        } catch (Exception e) {
            log.error("从数据库检测僵尸任务失败", e);
            return new ArrayList<>();
        }
    }
}
