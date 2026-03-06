package com.example.aiend.common.enums;

import lombok.Getter;

/**
 * 任务类型枚举
 *
 * @author AI-End
 * @since 2025-12-26
 */
@Getter
public enum TaskTypeEnum {
    
    /**
     * 陪聊
     */
    CHAT("CHAT", "陪聊"),
        
    /**
     * 保洁
     */
    CLEANING("CLEANING", "保洁"),
        
    /**
     * 跑腿
     */
    ERRAND("ERRAND", "跑腿"),
        
    /**
     * 医疗
     */
    MEDICAL("MEDICAL", "医疗"),
        
    /**
     * 其他
     */
    OTHER("OTHER", "其他");
    
    /**
     * 类型编码
     */
    private final String code;
    
    /**
     * 类型描述
     */
    private final String desc;
    
    TaskTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    /**
     * 根据编码获取枚举
     *
     * @param code 类型编码
     * @return 对应的枚举值，未找到返回null
     */
    public static TaskTypeEnum fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        for (TaskTypeEnum type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }
}
