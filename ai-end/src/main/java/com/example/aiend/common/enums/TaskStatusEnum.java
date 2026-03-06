package com.example.aiend.common.enums;

import lombok.Getter;

/**
 * 任务状态枚举
 * 定义任务全生命周期的六种状态
 *
 * @author AI-End
 * @since 2025-12-26
 */
@Getter
public enum TaskStatusEnum {
    
    /**
     * 待接单 - 任务发布后等待志愿者接取
     */
    PENDING(0, "待接单"),
    
    /**
     * 进行中 - 志愿者已接单，正在执行任务
     */
    IN_PROGRESS(1, "进行中"),
    
    /**
     * 待验收 - 志愿者完成服务，等待发布者确认
     */
    WAITING_CONFIRM(2, "待验收"),
    
    /**
     * 已完成 - 发布者确认服务完成，任务结束
     */
    COMPLETED(3, "已完成"),
    
    /**
     * 申诉中 - 发布者或志愿者发起申诉，等待仲裁
     */
    APPEALING(4, "申诉中"),
    
    /**
     * 已取消 - 任务被取消（发布者取消或系统取消）
     */
    CANCELLED(5, "已取消");
    
    /**
     * 状态码
     */
    private final Integer code;
    
    /**
     * 状态描述
     */
    private final String desc;
    
    TaskStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    /**
     * 根据状态码获取枚举
     *
     * @param code 状态码
     * @return 对应的枚举值，未找到返回null
     */
    public static TaskStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TaskStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
    
    /**
     * 根据名称获取枚举
     *
     * @param name 枚举名称
     * @return 对应的枚举值，未找到返回null
     */
    public static TaskStatusEnum fromName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        try {
            return TaskStatusEnum.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
