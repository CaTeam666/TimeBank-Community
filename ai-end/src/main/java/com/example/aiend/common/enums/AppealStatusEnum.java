package com.example.aiend.common.enums;

import lombok.Getter;

/**
 * 申诉状态枚举
 *
 * @author AI-End
 * @since 2025-12-26
 */
@Getter
public enum AppealStatusEnum {
    
    /**
     * 待处理
     */
    PENDING(0, "待处理"),
    
    /**
     * 已通过
     */
    APPROVED(1, "已通过"),
    
    /**
     * 已驳回
     */
    REJECTED(2, "已驳回");
    
    /**
     * 状态码
     */
    private final Integer code;
    
    /**
     * 状态描述
     */
    private final String desc;
    
    AppealStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    /**
     * 根据状态码获取枚举
     *
     * @param code 状态码
     * @return 对应的枚举值，未找到返回null
     */
    public static AppealStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AppealStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
