package com.example.aiend.service;

import com.example.aiend.dto.request.UserRelationAuditDTO;
import com.example.aiend.dto.request.UserRelationQueryDTO;
import com.example.aiend.dto.response.PageResponseDTO;
import com.example.aiend.vo.UserRelationVO;

/**
 * 用户关系（亲情绑定）服务接口
 *
 * @author AI-End
 * @since 2025-12-25
 */
public interface UserRelationService {
    
    /**
     * 获取亲情绑定申请列表（支持分页、状态筛选、关键词搜索）
     *
     * @param queryDTO 查询条件
     * @return 分页列表
     */
    PageResponseDTO<UserRelationVO> getRelationList(UserRelationQueryDTO queryDTO);
    
    /**
     * 审核亲情绑定申请
     *
     * @param id 关系ID
     * @param auditDTO 审核DTO
     */
    void auditRelation(Long id, UserRelationAuditDTO auditDTO);
}
