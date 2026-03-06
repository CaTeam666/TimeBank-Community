package com.example.aiend.common.scheduler;

import com.example.aiend.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 任务延迟删除调度器
 * 使用 Redis ZSet 实现延迟队列，定时扫描并删除已取消的任务
 *
 * @author AI-End
 * @since 2025-12-31
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TaskDelayDeleteScheduler {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final TaskMapper taskMapper;
    
    /**
     * 延迟删除队列的 Redis Key
     * ZSet 结构：score 为删除时间戳，value 为任务ID
     */
    public static final String DELAY_DELETE_QUEUE_KEY = "task:delay:delete";
    
    /**
     * 默认延迟删除时间（10分钟，单位：毫秒）
     */
    public static final long DEFAULT_DELAY_MS = 10 * 60 * 1000;
    
    /**
     * 添加任务到延迟删除队列
     *
     * @param taskId  任务ID
     * @param delayMs 延迟时间（毫秒）
     */
    public void addToDelayDeleteQueue(Long taskId, long delayMs) {
        long deleteTime = System.currentTimeMillis() + delayMs;
        redisTemplate.opsForZSet().add(DELAY_DELETE_QUEUE_KEY, String.valueOf(taskId), deleteTime);
        log.info("任务[{}]已加入延迟删除队列，将在{}毫秒后删除", taskId, delayMs);
    }
    
    /**
     * 添加任务到延迟删除队列（使用默认延迟时间10分钟）
     *
     * @param taskId 任务ID
     */
    public void addToDelayDeleteQueue(Long taskId) {
        addToDelayDeleteQueue(taskId, DEFAULT_DELAY_MS);
    }
    
    /**
     * 从延迟删除队列中移除任务
     * 用于任务状态变更后取消删除计划
     *
     * @param taskId 任务ID
     */
    public void removeFromDelayDeleteQueue(Long taskId) {
        Long removed = redisTemplate.opsForZSet().remove(DELAY_DELETE_QUEUE_KEY, String.valueOf(taskId));
        if (removed != null && removed > 0) {
            log.info("任务[{}]已从延迟删除队列中移除", taskId);
        }
    }
    
    /**
     * 定时扫描延迟删除队列
     * 每30秒执行一次，检查是否有到期需要删除的任务
     */
    @Scheduled(fixedRate = 30000)
    public void processDelayDeleteQueue() {
        try {
            long now = System.currentTimeMillis();
            
            // 获取所有到期的任务（score <= 当前时间）
            Set<Object> expiredTasks = redisTemplate.opsForZSet()
                    .rangeByScore(DELAY_DELETE_QUEUE_KEY, 0, now);
            
            if (expiredTasks == null || expiredTasks.isEmpty()) {
                return;
            }
            
            log.info("发现{}个到期需要删除的任务", expiredTasks.size());
            
            for (Object taskIdObj : expiredTasks) {
                Long taskId = Long.parseLong(taskIdObj.toString());
                try {
                    // 执行逻辑删除
                    int deleted = taskMapper.deleteById(taskId);
                    if (deleted > 0) {
                        log.info("任务[{}]已被自动删除", taskId);
                    } else {
                        log.warn("任务[{}]删除失败或已不存在", taskId);
                    }
                    
                    // 从队列中移除
                    redisTemplate.opsForZSet().remove(DELAY_DELETE_QUEUE_KEY, taskIdObj);
                    
                } catch (Exception e) {
                    log.error("删除任务[{}]时发生异常", taskId, e);
                }
            }
            
        } catch (Exception e) {
            log.error("处理延迟删除队列时发生异常", e);
        }
    }
}
