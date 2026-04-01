package com.example.aiend.common.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aiend.common.enums.TaskStatusEnum;
import com.example.aiend.dto.request.SystemSettingsDTO;
import com.example.aiend.entity.Task;
import com.example.aiend.entity.TaskEvidence;
import com.example.aiend.mapper.TaskEvidenceMapper;
import com.example.aiend.mapper.TaskMapper;
import com.example.aiend.service.SettingsService;
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
    private final SettingsService settingsService;
    private final TaskEvidenceMapper taskEvidenceMapper;

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
            // 从系统配置动态读取超时时长
            SystemSettingsDTO settings = settingsService.getSettings();
            int timeoutHours = settings.getZombieTaskTimeoutHours() != null ? settings.getZombieTaskTimeoutHours() : 24;
            
            LocalDateTime now = LocalDateTime.now();
            // 超时阈值 = 当前时间 - 配置的超时小时数
            LocalDateTime timeoutThreshold = now.minusHours(timeoutHours);
            
            log.info("僵尸任务超时阈值：{}小时，截止时间：{}", timeoutHours, timeoutThreshold);
            
            // 1. 先检查Redis缓存中的任务（获取待接单任务ID列表，用于日志记录）
            checkRedisForZombieTasks(now, timeoutThreshold);

            // 2. 查询并处理超时未接单的任务（状态为 0/1，超时自动取消）
            List<Task> zombieTasks = checkDatabaseForZombieTasks(timeoutThreshold);
            int cancelledCount = 0;
            for (Task task : zombieTasks) {
                boolean success = anomalyService.processZombieTask(task);
                if (success) {
                    cancelledCount++;
                }
            }
            log.info("僵尸任务检测完成，超时任务数：{}，自动取消任务数：{}", zombieTasks.size(), cancelledCount);

            // 3. 查询并处理超时未验收的任务（状态为 2，超时 1 小时自动完成）
            LocalDateTime autoConfirmThreshold = now.minusHours(1);
            List<Task> pendingConfirmTasks = checkDatabaseForAutoConfirmTasks(autoConfirmThreshold);
            int autoConfirmedCount = 0;
            for (Task task : pendingConfirmTasks) {
                boolean success = anomalyService.processAutoConfirmTask(task);
                if (success) {
                    autoConfirmedCount++;
                }
            }
            log.info("自动验收检测完成，待验收超时任务数：{}，自动完成任务数：{}", pendingConfirmTasks.size(), autoConfirmedCount);

            // 5. 查询并处理进行中由于未签到而超时的任务（状态为 1，已过服务时间）
            List<Task> pendingNoCheckInTasks = checkDatabaseForNoCheckInTasks(now);
            int autoCancelledCount = 0;
            for (Task task : pendingNoCheckInTasks) {
                boolean success = anomalyService.processNoCheckInTask(task);
                if (success) {
                    autoCancelledCount++;
                }
            }
            log.info("未签到取消扫描完成，未签到超时任务数：{}，自动取消任务数：{}", pendingNoCheckInTasks.size(), autoCancelledCount);

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
     * 从数据库中检测超时未接单的僵尸任务
     * 查找：状态为待接单、无志愿者接单、创建时间早于超时阈值的任务
     *
     * @param timeoutThreshold 超时阈值时间（创建时间早于此时间的任务视为僵尸任务）
     * @return 异常任务列表
     */
    private List<Task> checkDatabaseForZombieTasks(LocalDateTime timeoutThreshold) {
        try {
            LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Task::getStatus, TaskStatusEnum.PENDING.getCode())  // 待接单状态
                    .isNull(Task::getVolunteerId)  // 无志愿者接单
                    .le(Task::getCreateTime, timeoutThreshold)  // 创建时间 <= 超时阈值
                    .orderByAsc(Task::getCreateTime);

            List<Task> zombieTasks = taskMapper.selectList(queryWrapper);
            log.info("从数据库中检测到 {} 个超时未接单的僵尸任务", zombieTasks.size());

            return zombieTasks;

        } catch (Exception e) {
            log.error("从数据库检测僵尸任务失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 从数据库中检测超时未验收的任务
     * 查找：状态为待验收（2）、更新时间早于超时阈值的任务
     *
     * @param threshold 超时阈值时间（更新时间早于此时间的任务视为超时）
     * @return 待验收超时任务列表
     */
    private List<Task> checkDatabaseForAutoConfirmTasks(LocalDateTime threshold) {
        try {
            LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Task::getStatus, TaskStatusEnum.WAITING_CONFIRM.getCode())
                    .le(Task::getUpdateTime, threshold)
                    .orderByAsc(Task::getUpdateTime);

            List<Task> tasks = taskMapper.selectList(queryWrapper);
            log.info("从数据库中检测到 {} 个超时未验收的任务", tasks.size());
            return tasks;
        } catch (Exception e) {
            log.error("从数据库检测待验收超时任务失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 从数据库中检测超时未签到的进行中任务
     * 查找：状态为进行中（1）、服务时间早于当前时间、且存证表中无签到记录的任务
     *
     * @param now 当前时间
     * @return 未签到超时任务列表
     */
    private List<Task> checkDatabaseForNoCheckInTasks(LocalDateTime now) {
        try {
            // 1. 查询所有状态为进行中(1)且服务时间已过的任务
            LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Task::getStatus, TaskStatusEnum.IN_PROGRESS.getCode())
                    .le(Task::getServiceTime, now)
                    .orderByAsc(Task::getServiceTime);

            List<Task> candidateTasks = taskMapper.selectList(queryWrapper);
            List<Task> resultTasks = new ArrayList<>();

            // 2. 过滤掉已有签到记录的任务
            for (Task task : candidateTasks) {
                LambdaQueryWrapper<TaskEvidence> evidenceWrapper = new LambdaQueryWrapper<>();
                evidenceWrapper.eq(TaskEvidence::getTaskId, task.getId())
                        .isNotNull(TaskEvidence::getCheckInTime);
                
                Long count = taskEvidenceMapper.selectCount(evidenceWrapper);
                if (count == 0) {
                    resultTasks.add(task);
                }
            }

            log.info("从数据库中检测到 {} 个未签到且超时的进行中任务", resultTasks.size());
            return resultTasks;
        } catch (Exception e) {
            log.error("从数据库检测未签到超时任务失败", e);
            return new ArrayList<>();
        }
    }
}
