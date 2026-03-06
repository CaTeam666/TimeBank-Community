package com.example.aiend.dto.request;

import lombok.Data;

/**
 * 用户查询 DTO
 *
 * @author AI-End
 * @since 2025-12-21
 */
@Data
public class UserQueryDTO {
    
    /**
     * 页码，默认 1
     */
    private Integer page = 1;
    
    /**
     * 每页数量，默认 10
     */
    private Integer pageSize = 10;
    
    /**
     * 搜索关键词 (匹配昵称、手机号或真实姓名)
     */
    private String keyword;
    
    /**
     * 角色筛选 (ELDER, VOLUNTEER, CHILD_AGENT, ADMIN)
     */
    private String role;
    
    /**
     * 状态筛选 (NORMAL, FROZEN)
     */
    private String status;
}
