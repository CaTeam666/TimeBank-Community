package com.example.aiend.common.enums;

import lombok.Getter;

/**
 * 消息类型枚举
 * 定义系统中所有类型的消息及其路由
 *
 * @author AI-End
 * @since 2026-02-07
 */
@Getter
public enum MessageTypeEnum {
    
    /**
     * 亲情绑定消息
     * 当管理员审核通过后，推送给老人确认绑定
     */
    FAMILY_BIND("FAMILY_BIND", "亲情绑定", "/pages/family/pending-requests"),
    
    /**
     * 任务验收消息
     * 当志愿者提交任务完成凭证后，推送给发布者进行验收
     */
    TASK_VERIFY("TASK_VERIFY", "任务验收", "/pages/task/verify");
    
    /**
     * 消息类型编码
     */
    private final String code;
    
    /**
     * 消息类型名称
     */
    private final String name;
    
    /**
     * 前端跳转路由
     */
    private final String route;
    
    /**
     * 构造函数
     *
     * @param code 消息类型编码
     * @param name 消息类型名称
     * @param route 前端跳转路由
     */
    MessageTypeEnum(String code, String name, String route) {
        this.code = code;
        this.name = name;
        this.route = route;
    }
    
    /**
     * 根据类型编码获取枚举
     *
     * @param code 类型编码
     * @return 对应的枚举，如果不存在则返回null
     */
    public static MessageTypeEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (MessageTypeEnum type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
