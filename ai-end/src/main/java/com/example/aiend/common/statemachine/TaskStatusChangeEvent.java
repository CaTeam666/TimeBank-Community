package com.example.aiend.common.statemachine;

import com.example.aiend.common.enums.TaskStatusEnum;
import org.springframework.context.ApplicationEvent;

/**
 * 任务状态变更事件
 * 用于状态变更后的异步通知处理
 *
 * @author AI-End
 * @since 2025-12-31
 */
public class TaskStatusChangeEvent extends ApplicationEvent {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 任务ID
     */
    private final Long taskId;
    
    /**
     * 变更前状态
     */
    private final TaskStatusEnum fromStatus;
    
    /**
     * 变更后状态
     */
    private final TaskStatusEnum toStatus;
    
    /**
     * 操作人ID
     */
    private final Long operatorId;
    
    /**
     * 操作人角色（publisher:发布者, volunteer:志愿者, admin:管理员）
     */
    private final String operatorRole;
    
    /**
     * 变更原因/备注
     */
    private final String reason;
    
    /**
     * 变更时间戳
     */
    private final long timestamp;
    
    /**
     * 构造函数
     *
     * @param source       事件源
     * @param taskId       任务ID
     * @param fromStatus   变更前状态
     * @param toStatus     变更后状态
     * @param operatorId   操作人ID
     * @param operatorRole 操作人角色
     * @param reason       变更原因
     */
    public TaskStatusChangeEvent(Object source, Long taskId, TaskStatusEnum fromStatus,
                                  TaskStatusEnum toStatus, Long operatorId,
                                  String operatorRole, String reason) {
        super(source);
        this.taskId = taskId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.operatorId = operatorId;
        this.operatorRole = operatorRole;
        this.reason = reason;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 获取状态变更描述
     *
     * @return 变更描述
     */
    public String getTransitionDescription() {
        String event = TaskStateTransition.getTransitionEvent(fromStatus, toStatus);
        return String.format("任务[%d]: %s -> %s (%s)",
                taskId,
                fromStatus.getDesc(),
                toStatus.getDesc(),
                event != null ? event : "状态变更");
    }
    
    // ===== Getter 方法 =====
    
    public Long getTaskId() {
        return taskId;
    }
    
    public TaskStatusEnum getFromStatus() {
        return fromStatus;
    }
    
    public TaskStatusEnum getToStatus() {
        return toStatus;
    }
    
    public Long getOperatorId() {
        return operatorId;
    }
    
    public String getOperatorRole() {
        return operatorRole;
    }
    
    public String getReason() {
        return reason;
    }
    
    public long getEventTimestamp() {
        return timestamp;
    }
}
