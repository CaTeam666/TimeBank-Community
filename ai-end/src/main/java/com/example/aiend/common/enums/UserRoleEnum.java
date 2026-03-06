package com.example.aiend.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户角色枚举
 *
 * @author AI-End
 * @since 2025-12-19
 */
@Getter
@AllArgsConstructor
public enum UserRoleEnum {
    
    /**
     * 管理员
     */
    ADMIN("ADMIN", "管理员"),
    
    /**
     * 志愿者
     */
    VOLUNTEER("VOLUNTEER", "志愿者"),
    
    /**
     * 老人
     */
    ELDER("ELDER", "老人"),
    
    /**
     * 子女代理人
     */
    CHILD_AGENT("CHILD_AGENT", "子女代理人");
    
    private final String code;
    private final String desc;
    
    /**
     * 根据 code 获取枚举
     *
     * @param code 角色代码
     * @return 用户角色枚举
     */
    public static UserRoleEnum fromCode(String code) {
        for (UserRoleEnum role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("未知角色代码: " + code);
    }
}
