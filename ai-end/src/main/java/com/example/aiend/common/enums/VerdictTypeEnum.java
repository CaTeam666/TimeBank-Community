package com.example.aiend.common.enums;

import lombok.Getter;

/**
 * 裁决类型枚举
 *
 * @author AI-End
 * @since 2025-12-26
 */
@Getter
public enum VerdictTypeEnum {
    
    /**
     * 驳回/维持原判
     */
    REJECT("REJECT", "驳回"),
    
    /**
     * 判给志愿者/强制结算
     */
    TO_VOLUNTEER("TO_VOLUNTEER", "判给志愿者"),
    
    /**
     * 判给发布者/取消订单退款
     */
    TO_PUBLISHER("TO_PUBLISHER", "判给发布者");
    
    /**
     * 裁决类型编码
     */
    private final String code;
    
    /**
     * 裁决类型描述
     */
    private final String desc;
    
    VerdictTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    /**
     * 根据编码获取枚举
     *
     * @param code 裁决类型编码
     * @return 对应的枚举值，未找到返回null
     */
    public static VerdictTypeEnum fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        for (VerdictTypeEnum type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }
}
