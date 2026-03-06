package com.example.aiend.service;

import com.example.aiend.dto.request.RankingLogQueryDTO;
import com.example.aiend.dto.request.RankingLogRetryDTO;
import com.example.aiend.dto.response.PageResponseDTO;
import com.example.aiend.vo.RankingLogVO;

/**
 * 排名奖励服务接口
 *
 * @author AI-End
 * @since 2025-12-27
 */
public interface RankingService {
    
    /**
     * 获取奖励发放日志
     * 分页查询奖励发放日志，支持按期数筛选
     *
     * @param queryDTO 查询条件
     * @return 分页日志列表
     */
    PageResponseDTO<RankingLogVO> getRankingLogs(RankingLogQueryDTO queryDTO);
    
    /**
     * 手动触发补发
     * 对发放失败的奖励进行补发
     *
     * @param retryDTO 补发请求参数
     */
    void retryDistribution(RankingLogRetryDTO retryDTO);
}
