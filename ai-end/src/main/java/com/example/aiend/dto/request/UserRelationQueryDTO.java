package com.example.aiend.dto.request;

import lombok.Data;

/**
 * 用户关系（亲情绑定）查询 DTO
 *
 * @author AI-End
 * @since 2025-12-25
 */
@Data
public class UserRelationQueryDTO {
    
    /**
     * 页码，默认 1
     */
    private Integer page = 1;
    
    /**
     * 每页数量，默认 10
     */
    private Integer pageSize = 10;
    
    /**
     * 状态筛选（0:待审核 1:已通过 2:已拒绝）
     */
    private Integer status;
    
    /**
     * 搜索关键字（申请人/目标人姓名或手机号）
     */
    private String keyword;
}
