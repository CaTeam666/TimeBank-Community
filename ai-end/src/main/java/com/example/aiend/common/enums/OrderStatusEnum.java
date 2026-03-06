package com.example.aiend.common.enums;

import lombok.Getter;

/**
 * 订单状态枚举
 *
 * @author AI-End Team
 * @since 2024-12-27
 */
@Getter
public enum OrderStatusEnum {

    /**
     * 待核销
     */
    PENDING(0, "PENDING", "待核销"),

    /**
     * 已核销
     */
    COMPLETED(1, "COMPLETED", "已核销"),

    /**
     * 已取消
     */
    CANCELLED(2, "CANCELLED", "已取消");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 状态标识
     */
    private final String status;

    /**
     * 状态描述
     */
    private final String desc;

    OrderStatusEnum(Integer code, String status, String desc) {
        this.code = code;
        this.status = status;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举
     *
     * @param code 状态码
     * @return 枚举实例
     */
    public static OrderStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OrderStatusEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 根据状态标识获取枚举
     *
     * @param status 状态标识
     * @return 枚举实例
     */
    public static OrderStatusEnum fromStatus(String status) {
        if (status == null) {
            return null;
        }
        for (OrderStatusEnum value : values()) {
            if (value.getStatus().equals(status)) {
                return value;
            }
        }
        return null;
    }
}
