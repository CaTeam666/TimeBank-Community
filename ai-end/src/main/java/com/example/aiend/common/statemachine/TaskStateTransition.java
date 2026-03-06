package com.example.aiend.common.statemachine;

import com.example.aiend.common.enums.TaskStatusEnum;
import lombok.Getter;

import java.util.*;

/**
 * 任务状态转换规则定义
 * 定义所有合法的状态转换路径
 *
 * @author AI-End
 * @since 2025-12-31
 */
@Getter
public class TaskStateTransition {
    
    /**
     * 状态转换规则映射
     * key: 源状态, value: 该状态可转换到的目标状态集合
     */
    private static final Map<TaskStatusEnum, Set<TaskStatusEnum>> TRANSITIONS;
    
    /**
     * 状态转换事件描述
     * key: "源状态_目标状态", value: 事件描述
     */
    private static final Map<String, String> TRANSITION_EVENTS;
    
    static {
        // 初始化状态转换规则
        TRANSITIONS = new EnumMap<>(TaskStatusEnum.class);
        TRANSITION_EVENTS = new HashMap<>();
        
        // ===== 待接单(0) 可转换到的状态 =====
        // 待接单 -> 进行中（志愿者接单）
        // 待接单 -> 已取消（发布者取消）
        Set<TaskStatusEnum> pendingTransitions = EnumSet.of(
                TaskStatusEnum.IN_PROGRESS,
                TaskStatusEnum.CANCELLED
        );
        TRANSITIONS.put(TaskStatusEnum.PENDING, pendingTransitions);
        TRANSITION_EVENTS.put(buildKey(TaskStatusEnum.PENDING, TaskStatusEnum.IN_PROGRESS), "志愿者接单");
        TRANSITION_EVENTS.put(buildKey(TaskStatusEnum.PENDING, TaskStatusEnum.CANCELLED), "发布者取消任务");
        
        // ===== 进行中(1) 可转换到的状态 =====
        // 进行中 -> 待验收（志愿者提交完成）
        // 进行中 -> 已取消（双方协商取消）
        // 进行中 -> 申诉中（志愿者发起申诉）
        Set<TaskStatusEnum> inProgressTransitions = EnumSet.of(
                TaskStatusEnum.WAITING_CONFIRM,
                TaskStatusEnum.CANCELLED,
                TaskStatusEnum.APPEALING
        );
        TRANSITIONS.put(TaskStatusEnum.IN_PROGRESS, inProgressTransitions);
        TRANSITION_EVENTS.put(buildKey(TaskStatusEnum.IN_PROGRESS, TaskStatusEnum.WAITING_CONFIRM), "志愿者提交完成");
        TRANSITION_EVENTS.put(buildKey(TaskStatusEnum.IN_PROGRESS, TaskStatusEnum.CANCELLED), "协商取消任务");
        TRANSITION_EVENTS.put(buildKey(TaskStatusEnum.IN_PROGRESS, TaskStatusEnum.APPEALING), "志愿者发起申诉");
        
        // ===== 待验收(2) 可转换到的状态 =====
        // 待验收 -> 已完成（发布者确认完成）
        // 待验收 -> 申诉中（发布者拒绝验收，发起申诉）
        Set<TaskStatusEnum> waitingConfirmTransitions = EnumSet.of(
                TaskStatusEnum.COMPLETED,
                TaskStatusEnum.APPEALING
        );
        TRANSITIONS.put(TaskStatusEnum.WAITING_CONFIRM, waitingConfirmTransitions);
        TRANSITION_EVENTS.put(buildKey(TaskStatusEnum.WAITING_CONFIRM, TaskStatusEnum.COMPLETED), "发布者确认完成");
        TRANSITION_EVENTS.put(buildKey(TaskStatusEnum.WAITING_CONFIRM, TaskStatusEnum.APPEALING), "发布者拒绝验收");
        
        // ===== 已完成(3) - 终态，不可转换 =====
        TRANSITIONS.put(TaskStatusEnum.COMPLETED, EnumSet.noneOf(TaskStatusEnum.class));
        
        // ===== 已取消(4) - 终态，不可转换 =====
        TRANSITIONS.put(TaskStatusEnum.CANCELLED, EnumSet.noneOf(TaskStatusEnum.class));
        
        // ===== 申诉中(5) 可转换到的状态 =====
        // 申诉中 -> 已完成（仲裁判定完成）
        // 申诉中 -> 已取消（仲裁判定取消）
        // 申诉中 -> 进行中（仲裁判定继续执行）
        Set<TaskStatusEnum> appealingTransitions = EnumSet.of(
                TaskStatusEnum.COMPLETED,
                TaskStatusEnum.CANCELLED,
                TaskStatusEnum.IN_PROGRESS
        );
        TRANSITIONS.put(TaskStatusEnum.APPEALING, appealingTransitions);
        TRANSITION_EVENTS.put(buildKey(TaskStatusEnum.APPEALING, TaskStatusEnum.COMPLETED), "仲裁判定完成");
        TRANSITION_EVENTS.put(buildKey(TaskStatusEnum.APPEALING, TaskStatusEnum.CANCELLED), "仲裁判定取消");
        TRANSITION_EVENTS.put(buildKey(TaskStatusEnum.APPEALING, TaskStatusEnum.IN_PROGRESS), "仲裁判定继续执行");
    }
    
    /**
     * 私有构造方法，防止实例化
     */
    private TaskStateTransition() {
    }
    
    /**
     * 检查状态转换是否合法
     *
     * @param from 源状态
     * @param to   目标状态
     * @return true-合法，false-非法
     */
    public static boolean canTransition(TaskStatusEnum from, TaskStatusEnum to) {
        if (from == null || to == null) {
            return false;
        }
        Set<TaskStatusEnum> allowedTargets = TRANSITIONS.get(from);
        return allowedTargets != null && allowedTargets.contains(to);
    }
    
    /**
     * 获取指定状态可转换的目标状态列表
     *
     * @param from 源状态
     * @return 可转换到的状态集合
     */
    public static Set<TaskStatusEnum> getAllowedTransitions(TaskStatusEnum from) {
        if (from == null) {
            return EnumSet.noneOf(TaskStatusEnum.class);
        }
        Set<TaskStatusEnum> transitions = TRANSITIONS.get(from);
        return transitions != null ? EnumSet.copyOf(transitions) : EnumSet.noneOf(TaskStatusEnum.class);
    }
    
    /**
     * 获取状态转换事件描述
     *
     * @param from 源状态
     * @param to   目标状态
     * @return 事件描述，未定义返回null
     */
    public static String getTransitionEvent(TaskStatusEnum from, TaskStatusEnum to) {
        return TRANSITION_EVENTS.get(buildKey(from, to));
    }
    
    /**
     * 判断是否为终态（不可再转换的状态）
     *
     * @param status 状态
     * @return true-是终态，false-不是终态
     */
    public static boolean isFinalState(TaskStatusEnum status) {
        if (status == null) {
            return false;
        }
        Set<TaskStatusEnum> transitions = TRANSITIONS.get(status);
        return transitions == null || transitions.isEmpty();
    }
    
    /**
     * 获取所有终态
     *
     * @return 终态集合
     */
    public static Set<TaskStatusEnum> getFinalStates() {
        return EnumSet.of(TaskStatusEnum.COMPLETED, TaskStatusEnum.CANCELLED);
    }
    
    /**
     * 构建转换规则的key
     */
    private static String buildKey(TaskStatusEnum from, TaskStatusEnum to) {
        return from.name() + "_" + to.name();
    }
}
