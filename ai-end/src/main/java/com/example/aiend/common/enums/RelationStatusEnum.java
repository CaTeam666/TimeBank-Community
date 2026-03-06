package com.example.aiend.common.enums;

import lombok.Getter;

/**
 * 亲情关系状态枚举
 * 定义用户关系绑定的各种状态及其流转
 *
 * @author AI-End
 * @since 2026-02-07
 */
@Getter
public enum RelationStatusEnum {
    
    /**
     * 待管理员审核
     * 子女发起申请后的初始状态，需要后台管理员审核资料
     */
    PENDING_ADMIN_AUDIT(0, "待管理员审核"),
    
    /**
     * 待老人确认
     * 管理员审核通过后，推送到老人端等待老人确认
     */
    PENDING_ELDER_CONFIRM(1, "待老人确认"),
    
    /**
     * 已绑定
     * 老人点击同意后，正式生效的状态
     */
    BOUND(2, "已绑定"),
    
    /**
     * 已拒绝
     * 管理员驳回或老人拒绝的状态
     */
    REJECTED(3, "已拒绝");
    
    /**
     * 状态码
     */
    private final Integer code;
    
    /**
     * 状态描述
     */
    private final String description;
    
    /**
     * 构造函数
     *
     * @param code 状态码
     * @param description 状态描述
     */
    RelationStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 根据状态码获取枚举
     *
     * @param code 状态码
     * @return 对应的枚举，如果不存在则返回null
     */
    public static RelationStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (RelationStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
    
    /**
     * 判断是否为待审核状态（包含待管理员审核和待老人确认）
     *
     * @return true表示待审核状态
     */
    public boolean isPending() {
        return this == PENDING_ADMIN_AUDIT || this == PENDING_ELDER_CONFIRM;
    }
    
    /**
     * 判断是否为已绑定状态
     *
     * @return true表示已绑定
     */
    public boolean isBound() {
        return this == BOUND;
    }
    
    /**
     * 判断是否为已拒绝状态
     *
     * @return true表示已拒绝
     */
    public boolean isRejected() {
        return this == REJECTED;
    }
}
