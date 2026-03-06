package com.example.aiend.service;

import com.example.aiend.dto.request.ArbitrationQueryDTO;
import com.example.aiend.dto.request.ArbitrationVerdictDTO;
import com.example.aiend.dto.response.PageResponseDTO;
import com.example.aiend.vo.ArbitrationDetailVO;
import com.example.aiend.vo.ArbitrationListVO;

/**
 * 仲裁服务接口
 *
 * @author AI-End
 * @since 2025-12-26
 */
public interface ArbitrationService {
    
    /**
     * 获取仲裁列表
     *
     * @param queryDTO 查询条件
     * @return 分页仲裁列表
     */
    PageResponseDTO<ArbitrationListVO> getArbitrationList(ArbitrationQueryDTO queryDTO);
    
    /**
     * 获取仲裁详情
     *
     * @param id 仲裁单ID
     * @return 仲裁详情
     */
    ArbitrationDetailVO getArbitrationDetail(String id);
    
    /**
     * 提交裁决
     *
     * @param verdictDTO 裁决请求参数
     * @param handlerId 处理人ID（可为null）
     */
    void submitVerdict(ArbitrationVerdictDTO verdictDTO, Long handlerId);
}
