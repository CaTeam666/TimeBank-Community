package com.example.aiend.dto.request;

import lombok.Data;

/**
 * 订单查询请求DTO
 *
 * @author AI-End Team
 * @since 2024-12-27
 */
@Data
public class OrderQueryDTO {

    /**
     * 状态筛选 (ALL, PENDING, COMPLETED, CANCELLED)
     */
    private String status = "ALL";

    /**
     * 搜索关键字 (订单号/志愿者名)
     */
    private String keyword;

    /**
     * 页码，默认1
     */
    private Integer page = 1;

    /**
     * 每页数量，默认10
     */
    private Integer pageSize = 10;
}
