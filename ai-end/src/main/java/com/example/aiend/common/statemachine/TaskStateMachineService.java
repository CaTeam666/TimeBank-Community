package com.example.aiend.common.statemachine;

import com.example.aiend.common.enums.TaskStatusEnum;

import java.util.Set;

/**
 * 任务状态机服务接口
 * 提供任务状态流转的核心功能
 *
 * @author AI-End
 * @since 2025-12-31
 */
public interface TaskStateMachineService {
    
    /**
     * 执行状态变更
     * 校验转换合法性并更新数据库状态
     *
     * @param taskId       任务ID
     * @param toStatus     目标状态
     * @param operatorId   操作人ID
     * @param operatorRole 操作人角色（publisher/volunteer/admin）
     * @param reason       变更原因
     * @return true-变更成功，false-变更失败
     */
    boolean changeStatus(Long taskId, TaskStatusEnum toStatus, Long operatorId, String operatorRole, String reason);
    
    /**
     * 志愿者接单
     * 待接单(0) -> 进行中(1)
     *
     * @param taskId      任务ID
     * @param volunteerId 志愿者ID
     * @return true-接单成功，false-接单失败
     */
    boolean acceptTask(Long taskId, Long volunteerId);
    
    /**
     * 志愿者提交完成
     * 进行中(1) -> 待验收(2)
     *
     * @param taskId      任务ID
     * @param volunteerId 志愿者ID
     * @return true-提交成功，false-提交失败
     */
    boolean submitComplete(Long taskId, Long volunteerId);
    
    /**
     * 发布者确认完成
     * 待验收(2) -> 已完成(3)
     *
     * @param taskId      任务ID
     * @param publisherId 发布者ID
     * @return true-确认成功，false-确认失败
     */
    boolean confirmComplete(Long taskId, Long publisherId);
    
    /**
     * 取消任务
     * 待接单(0)/进行中(1) -> 已取消(4)
     *
     * @param taskId     任务ID
     * @param operatorId 操作人ID
     * @param reason     取消原因
     * @return true-取消成功，false-取消失败
     */
    boolean cancelTask(Long taskId, Long operatorId, String reason);
    
    /**
     * 发起申诉
     * 进行中(1)/待验收(2) -> 申诉中(5)
     *
     * @param taskId     任务ID
     * @param operatorId 申诉人ID
     * @param reason     申诉原因
     * @return true-申诉成功，false-申诉失败
     */
    boolean appeal(Long taskId, Long operatorId, String reason);
    
    /**
     * 仲裁处理
     * 申诉中(5) -> 已完成(3)/已取消(4)/进行中(1)
     *
     * @param taskId   任务ID
     * @param adminId  管理员ID
     * @param toStatus 仲裁结果状态
     * @param reason   仲裁说明
     * @return true-处理成功，false-处理失败
     */
    boolean arbitrate(Long taskId, Long adminId, TaskStatusEnum toStatus, String reason);
    
    /**
     * 获取任务当前状态
     *
     * @param taskId 任务ID
     * @return 当前状态，任务不存在返回null
     */
    TaskStatusEnum getCurrentStatus(Long taskId);
    
    /**
     * 获取任务可执行的操作列表
     *
     * @param taskId 任务ID
     * @return 可转换到的状态集合
     */
    Set<TaskStatusEnum> getAvailableActions(Long taskId);
    
    /**
     * 检查状态转换是否合法
     *
     * @param taskId   任务ID
     * @param toStatus 目标状态
     * @return true-合法，false-非法
     */
    boolean canTransitionTo(Long taskId, TaskStatusEnum toStatus);
}
