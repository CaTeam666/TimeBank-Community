package com.example.aiend.dto.request;

import lombok.Data;

/**
 * 存证查询请求DTO
 *
 * @author AI-End Team
 * @since 2024-12-26
 */
@Data
public class EvidenceQueryDTO {

    /**
     * 页码，默认1
     */
    private Integer page = 1;

    /**
     * 每页条数，默认12
     */
    private Integer pageSize = 12;

    /**
     * 搜索关键词 (任务ID / 志愿者姓名 / 志愿者ID)
     */
    private String keyword;
}
