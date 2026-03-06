package com.example.aiend.service;

import com.example.aiend.dto.request.EvidenceQueryDTO;
import com.example.aiend.dto.response.PageResponseDTO;
import com.example.aiend.vo.EvidenceListVO;

/**
 * 存证服务接口
 *
 * @author AI-End Team
 * @since 2024-12-26
 */
public interface EvidenceService {

    /**
     * 获取存证列表
     *
     * @param queryDTO 查询参数
     * @return 分页存证列表
     */
    PageResponseDTO<EvidenceListVO> getEvidenceList(EvidenceQueryDTO queryDTO);
}
