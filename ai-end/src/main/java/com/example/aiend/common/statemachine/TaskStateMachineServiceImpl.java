package com.example.aiend.common.statemachine;

import com.example.aiend.common.enums.TaskStatusEnum;
import com.example.aiend.common.exception.BusinessException;
import com.example.aiend.entity.Task;
import com.example.aiend.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 任务状态机服务实现类
 * 管理任务状态的合法流转
 *
 * @author AI-End
 * @since 2025-12-31
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TaskStateMachineServiceImpl implements TaskStateMachineService {
    
    private final TaskMapper taskMapper;
    private final ApplicationEventPublisher eventPublisher;
    
    /**
     * 操作人角色常量
     */
    private static final String ROLE_PUBLISHER = "publisher";
    private static final String ROLE_VOLUNTEER = "volunteer";
    private static final String ROLE_ADMIN = "admin";
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changeStatus(Long taskId, TaskStatusEnum toStatus, Long operatorId, 
                                 String operatorRole, String reason) {
        log.info("状态变更请求 - 任务ID: {}, 目标状态: {}, 操作人: {}, 角色: {}", 
                taskId, toStatus, operatorId, operatorRole);
        
        // 1. 查询任务当前状态
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            log.warn("任务不存在，任务ID: {}", taskId);
            throw new BusinessException(404, "任务不存在");
        }
        
        TaskStatusEnum fromStatus = TaskStatusEnum.fromCode(task.getStatus());
        if (fromStatus == null) {
            log.error("任务状态异常，任务ID: {}, 状态码: {}", taskId, task.getStatus());
            throw new BusinessException(500, "任务状态异常");
        }
        
        // 2. 校验状态转换合法性
        if (!TaskStateTransition.canTransition(fromStatus, toStatus)) {
            String eventDesc = TaskStateTransition.getTransitionEvent(fromStatus, toStatus);
            log.warn("非法状态转换 - 任务ID: {}, {} -> {} ({})", 
                    taskId, fromStatus.getDesc(), toStatus.getDesc(), eventDesc);
            throw new BusinessException(400, 
                    String.format("不允许从[%s]转换到[%s]", fromStatus.getDesc(), toStatus.getDesc()));
        }
        
        // 3. 更新数据库状态
        task.setStatus(toStatus.getCode());
        task.setUpdateTime(LocalDateTime.now());
        int affectedRows = taskMapper.updateById(task);
        
        if (affectedRows > 0) {
            log.info("状态变更成功 - 任务ID: {}, {} -> {}", taskId, fromStatus.getDesc(), toStatus.getDesc());
            
            // 4. 发布状态变更事件
            publishStatusChangeEvent(taskId, fromStatus, toStatus, operatorId, operatorRole, reason);
            return true;
        } else {
            log.error("状态变更失败 - 任务ID: {}", taskId);
            return false;
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean acceptTask(Long taskId, Long volunteerId) {
        log.info("志愿者接单 - 任务ID: {}, 志愿者ID: {}", taskId, volunteerId);
        
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(404, "任务不存在");
        }
        
        // 校验当前状态
        TaskStatusEnum fromStatus = TaskStatusEnum.fromCode(task.getStatus());
        if (fromStatus != TaskStatusEnum.PENDING) {
            throw new BusinessException(400, "任务已被接单或已结束");
        }
        
        // 使用乐观锁更新
        int affectedRows = taskMapper.acceptTaskWithOptimisticLock(taskId, volunteerId);
        
        if (affectedRows > 0) {
            log.info("接单成功 - 任务ID: {}, 志愿者ID: {}", taskId, volunteerId);
            publishStatusChangeEvent(taskId, TaskStatusEnum.PENDING, TaskStatusEnum.IN_PROGRESS, 
                    volunteerId, ROLE_VOLUNTEER, "志愿者接单");
            return true;
        } else {
            log.warn("接单失败，任务可能已被其他人接走 - 任务ID: {}", taskId);
            return false;
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitComplete(Long taskId, Long volunteerId) {
        log.info("志愿者提交完成 - 任务ID: {}, 志愿者ID: {}", taskId, volunteerId);
        
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(404, "任务不存在");
        }
        
        // 校验是否为该任务的志愿者
        if (!volunteerId.equals(task.getVolunteerId())) {
            throw new BusinessException(403, "无权操作此任务");
        }
        
        // 校验当前状态
        TaskStatusEnum fromStatus = TaskStatusEnum.fromCode(task.getStatus());
        if (fromStatus != TaskStatusEnum.IN_PROGRESS) {
            throw new BusinessException(400, "任务状态不正确，无法提交完成");
        }
        
        return changeStatus(taskId, TaskStatusEnum.WAITING_CONFIRM, volunteerId, ROLE_VOLUNTEER, "志愿者提交完成");
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmComplete(Long taskId, Long publisherId) {
        log.info("发布者确认完成 - 任务ID: {}, 发布者ID: {}", taskId, publisherId);
        
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(404, "任务不存在");
        }
        
        // 校验是否为该任务的发布者
        if (!publisherId.equals(task.getPublisherId())) {
            throw new BusinessException(403, "无权操作此任务");
        }
        
        // 校验当前状态
        TaskStatusEnum fromStatus = TaskStatusEnum.fromCode(task.getStatus());
        if (fromStatus != TaskStatusEnum.WAITING_CONFIRM) {
            throw new BusinessException(400, "任务状态不正确，无法确认完成");
        }
        
        return changeStatus(taskId, TaskStatusEnum.COMPLETED, publisherId, ROLE_PUBLISHER, "发布者确认完成");
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelTask(Long taskId, Long operatorId, String reason) {
        log.info("取消任务 - 任务ID: {}, 操作人ID: {}, 原因: {}", taskId, operatorId, reason);
        
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(404, "任务不存在");
        }
        
        // 校验当前状态是否允许取消
        TaskStatusEnum fromStatus = TaskStatusEnum.fromCode(task.getStatus());
        if (!TaskStateTransition.canTransition(fromStatus, TaskStatusEnum.CANCELLED)) {
            throw new BusinessException(400, 
                    String.format("任务当前状态[%s]不允许取消", fromStatus.getDesc()));
        }
        
        // 判断操作人角色
        String role = ROLE_ADMIN;
        if (operatorId.equals(task.getPublisherId())) {
            role = ROLE_PUBLISHER;
        } else if (operatorId.equals(task.getVolunteerId())) {
            role = ROLE_VOLUNTEER;
        }
        
        return changeStatus(taskId, TaskStatusEnum.CANCELLED, operatorId, role, reason);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean appeal(Long taskId, Long operatorId, String reason) {
        log.info("发起申诉 - 任务ID: {}, 申诉人ID: {}, 原因: {}", taskId, operatorId, reason);
        
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(404, "任务不存在");
        }
        
        // 校验是否为任务相关方
        if (!operatorId.equals(task.getPublisherId()) && !operatorId.equals(task.getVolunteerId())) {
            throw new BusinessException(403, "无权对此任务发起申诉");
        }
        
        // 校验当前状态是否允许申诉
        TaskStatusEnum fromStatus = TaskStatusEnum.fromCode(task.getStatus());
        if (!TaskStateTransition.canTransition(fromStatus, TaskStatusEnum.APPEALING)) {
            throw new BusinessException(400, 
                    String.format("任务当前状态[%s]不允许发起申诉", fromStatus.getDesc()));
        }
        
        String role = operatorId.equals(task.getPublisherId()) ? ROLE_PUBLISHER : ROLE_VOLUNTEER;
        return changeStatus(taskId, TaskStatusEnum.APPEALING, operatorId, role, reason);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean arbitrate(Long taskId, Long adminId, TaskStatusEnum toStatus, String reason) {
        log.info("仲裁处理 - 任务ID: {}, 管理员ID: {}, 目标状态: {}, 说明: {}", 
                taskId, adminId, toStatus, reason);
        
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(404, "任务不存在");
        }
        
        // 校验当前状态必须是申诉中
        TaskStatusEnum fromStatus = TaskStatusEnum.fromCode(task.getStatus());
        if (fromStatus != TaskStatusEnum.APPEALING) {
            throw new BusinessException(400, "只有申诉中的任务才能进行仲裁");
        }
        
        // 校验目标状态是否为合法的仲裁结果
        if (!TaskStateTransition.canTransition(fromStatus, toStatus)) {
            throw new BusinessException(400, 
                    String.format("仲裁结果[%s]不是合法的状态", toStatus.getDesc()));
        }
        
        return changeStatus(taskId, toStatus, adminId, ROLE_ADMIN, "仲裁处理: " + reason);
    }
    
    @Override
    public TaskStatusEnum getCurrentStatus(Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            return null;
        }
        return TaskStatusEnum.fromCode(task.getStatus());
    }
    
    @Override
    public Set<TaskStatusEnum> getAvailableActions(Long taskId) {
        TaskStatusEnum currentStatus = getCurrentStatus(taskId);
        return TaskStateTransition.getAllowedTransitions(currentStatus);
    }
    
    @Override
    public boolean canTransitionTo(Long taskId, TaskStatusEnum toStatus) {
        TaskStatusEnum currentStatus = getCurrentStatus(taskId);
        return TaskStateTransition.canTransition(currentStatus, toStatus);
    }
    
    /**
     * 发布状态变更事件
     */
    private void publishStatusChangeEvent(Long taskId, TaskStatusEnum fromStatus, 
                                           TaskStatusEnum toStatus, Long operatorId, 
                                           String operatorRole, String reason) {
        TaskStatusChangeEvent event = new TaskStatusChangeEvent(
                this, taskId, fromStatus, toStatus, operatorId, operatorRole, reason);
        eventPublisher.publishEvent(event);
        log.debug("已发布状态变更事件: {}", event.getTransitionDescription());
    }
}
