package com.example.aiend.common.enums;

import lombok.Getter;

/**
 * 商品状态枚举
 *
 * @author AI-End Team
 * @since 2024-12-27
 */
@Getter
public enum ProductStatusEnum {

    /**
     * 下架
     */
    OFF_SHELF(0, "OFF_SHELF", "下架"),

    /**
     * 上架
     */
    ON_SHELF(1, "ON_SHELF", "上架");

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

    ProductStatusEnum(Integer code, String status, String desc) {
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
    public static ProductStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ProductStatusEnum value : values()) {
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
    public static ProductStatusEnum fromStatus(String status) {
        if (status == null) {
            return null;
        }
        for (ProductStatusEnum value : values()) {
            if (value.getStatus().equals(status)) {
                return value;
            }
        }
        return null;
    }
}
