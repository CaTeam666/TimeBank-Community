package com.example.aiend.service;

import com.example.aiend.dto.request.IdentityAuditQueryDTO;
import com.example.aiend.dto.request.IdentityAuditResultDTO;
import com.example.aiend.dto.response.PageResponseDTO;
import com.example.aiend.vo.IdentityAuditVO;

/**
 * 实名认证审核服务接口
 *
 * @author AI-End
 * @since 2025-12-24
 */
public interface IdentityAuditService {
    
    /**
     * 获取实名审核列表（支持分页、状态筛选）
     *
     * @param queryDTO 查询条件
     * @return 分页审核列表
     */
    PageResponseDTO<IdentityAuditVO> getAuditList(IdentityAuditQueryDTO queryDTO);
    
    /**
     * 获取审核详情
     *
     * @param id 审核任务ID
     * @return 审核详情
     */
    IdentityAuditVO getAuditDetail(Long id);
    
    /**
     * 提交审核结果
     *
     * @param id 审核任务ID
     * @param resultDTO 审核结果DTO
     */
    void submitAuditResult(Long id, IdentityAuditResultDTO resultDTO);
}
