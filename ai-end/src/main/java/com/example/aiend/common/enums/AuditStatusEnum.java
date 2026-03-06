package com.example.aiend.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审核状态枚举
 *
 * @author AI-End
 * @since 2025-12-24
 */
@Getter
@AllArgsConstructor
public enum AuditStatusEnum {
    
    /**
     * 待审核
     */
    PENDING(0, "PENDING", "待审核"),
    
    /**
     * 已通过
     */
    APPROVED(1, "APPROVED", "已通过"),
    
    /**
     * 已驳回
     */
    REJECTED(2, "REJECTED", "已驳回");
    
    private final Integer code;
    private final String status;
    private final String desc;
    
    /**
     * 根据 code 获取枚举
     *
     * @param code 状态代码
     * @return 审核状态枚举
     */
    public static AuditStatusEnum fromCode(Integer code) {
        for (AuditStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知审核状态代码: " + code);
    }
    
    /**
     * 根据 status 字符串获取枚举
     *
     * @param status 状态字符串
     * @return 审核状态枚举
     */
    public static AuditStatusEnum fromStatus(String status) {
        for (AuditStatusEnum auditStatus : values()) {
            if (auditStatus.getStatus().equals(status)) {
                return auditStatus;
            }
        }
        throw new IllegalArgumentException("未知审核状态: " + status);
    }
}
