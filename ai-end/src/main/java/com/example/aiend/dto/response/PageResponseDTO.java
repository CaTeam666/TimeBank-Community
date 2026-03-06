package com.example.aiend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 分页响应 DTO
 *
 * @author AI-End
 * @since 2025-12-21
 */
@Data
@Builder
public class PageResponseDTO<T> {
    
    /**
     * 数据列表
     */
    private List<T> list;
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 当前页码
     */
    private Integer page;
    
    /**
     * 每页数量
     */
    private Integer pageSize;
}
