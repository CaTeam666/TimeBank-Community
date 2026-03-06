package com.example.aiend.common.statemachine;

import com.example.aiend.common.enums.TaskStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 任务状态变更事件监听器
 * 处理状态变更后的异步业务逻辑
 *
 * @author AI-End
 * @since 2025-12-31
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TaskStatusChangeListener {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    // Redis缓存键前缀
    private static final String TASK_CATEGORY_PREFIX = "task:category:";
    private static final String TASK_ITEM_PREFIX = "task:item:";
    private static final String TASK_HALL_KEY = "task:hall:list";
    
    /**
     * 监听任务状态变更事件
     * 异步处理，不阻塞主业务流程
     *
     * @param event 状态变更事件
     */
    @Async
    @EventListener
    public void handleTaskStatusChange(TaskStatusChangeEvent event) {
        log.info("收到状态变更事件: {}", event.getTransitionDescription());
        
        try {
            // 根据不同的状态变更执行相应的业务逻辑
            TaskStatusEnum toStatus = event.getToStatus();
            
            switch (toStatus) {
                case IN_PROGRESS:
                    handleTaskAccepted(event);
                    break;
                case WAITING_CONFIRM:
                    handleTaskSubmitted(event);
                    break;
                case COMPLETED:
                    handleTaskCompleted(event);
                    break;
                case CANCELLED:
                    handleTaskCancelled(event);
                    break;
                case APPEALING:
                    handleTaskAppealing(event);
                    break;
                default:
                    log.debug("状态[{}]无需特殊处理", toStatus.getDesc());
            }
        } catch (Exception e) {
            log.error("处理状态变更事件失败: {}", event.getTransitionDescription(), e);
            // 事件处理失败不影响主业务，仅记录日志
        }
    }
    
    /**
     * 处理任务被接单
     * 从任务大厅缓存中移除该任务
     */
    private void handleTaskAccepted(TaskStatusChangeEvent event) {
        Long taskId = event.getTaskId();
        log.info("任务被接单，移除任务大厅缓存 - 任务ID: {}", taskId);
        
        try {
            // 删除单个任务缓存
            String itemKey = TASK_ITEM_PREFIX + taskId;
            redisTemplate.delete(itemKey);
            
            // 从所有分类ZSet中移除（遍历所有可能的分类）
            String[] categories = {"跑腿代办", "生活照料", "医疗健康", "家政维修", "陪伴聊天", "其他"};
            for (String category : categories) {
                String categoryKey = TASK_CATEGORY_PREFIX + category;
                redisTemplate.opsForZSet().remove(categoryKey, String.valueOf(taskId));
            }
            
            log.info("任务大厅缓存已更新 - 任务ID: {}", taskId);
        } catch (Exception e) {
            log.error("更新任务大厅缓存失败 - 任务ID: {}", taskId, e);
        }
    }
    
    /**
     * 处理任务提交完成
     * 可以发送通知给发布者
     */
    private void handleTaskSubmitted(TaskStatusChangeEvent event) {
        Long taskId = event.getTaskId();
        Long volunteerId = event.getOperatorId();
        log.info("任务已提交完成，等待发布者验收 - 任务ID: {}, 志愿者ID: {}", taskId, volunteerId);
        
        // TODO: 发送通知给发布者，提醒验收任务
    }
    
    /**
     * 处理任务完成
     * 执行时间币结算
     */
    private void handleTaskCompleted(TaskStatusChangeEvent event) {
        Long taskId = event.getTaskId();
        log.info("任务已完成，执行时间币结算 - 任务ID: {}", taskId);
        
        // TODO: 时间币结算逻辑
        // 1. 从发布者账户扣除时间币
        // 2. 向志愿者账户增加时间币
        // 3. 记录流水
    }
    
    /**
     * 处理任务取消
     * 执行退款逻辑（如有冻结）
     */
    private void handleTaskCancelled(TaskStatusChangeEvent event) {
        Long taskId = event.getTaskId();
        String reason = event.getReason();
        log.info("任务已取消 - 任务ID: {}, 原因: {}", taskId, reason);
        
        // 如果是待接单状态被取消，需要从缓存中移除
        if (event.getFromStatus() == TaskStatusEnum.PENDING) {
            handleTaskAccepted(event); // 复用移除缓存的逻辑
        }
        
        // TODO: 如果有冻结的时间币，执行退款逻辑
    }
    
    /**
     * 处理任务申诉
     * 通知管理员处理
     */
    private void handleTaskAppealing(TaskStatusChangeEvent event) {
        Long taskId = event.getTaskId();
        Long operatorId = event.getOperatorId();
        String reason = event.getReason();
        log.info("任务进入申诉状态 - 任务ID: {}, 申诉人ID: {}, 原因: {}", taskId, operatorId, reason);
        
        // TODO: 发送通知给管理员，提醒处理申诉
        // TODO: 创建申诉工单记录
    }
}
