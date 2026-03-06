package com.example.aiend.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 账号状态枚举
 *
 * @author AI-End
 * @since 2025-12-21
 */
@Getter
@AllArgsConstructor
public enum AccountStatusEnum {
    
    /**
     * 正常
     */
    NORMAL("NORMAL", "正常"),
    
    /**
     * 冻结
     */
    FROZEN("FROZEN", "冻结");
    
    private final String code;
    private final String desc;
    
    /**
     * 根据 code 获取枚举
     *
     * @param code 状态代码
     * @return 账号状态枚举
     */
    public static AccountStatusEnum fromCode(String code) {
        for (AccountStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知状态代码: " + code);
    }
}
