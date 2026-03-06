package com.example.aiend.service;

import com.example.aiend.dto.request.MissionForceCloseDTO;
import com.example.aiend.dto.request.MissionQueryDTO;
import com.example.aiend.dto.response.PageResponseDTO;
import com.example.aiend.vo.MissionDetailVO;
import com.example.aiend.vo.MissionVO;

/**
 * 任务监控服务接口
 *
 * @author AI-End
 * @since 2025-12-26
 */
public interface MissionService {
    
    /**
     * 获取任务列表
     *
     * @param queryDTO 查询条件
     * @return 分页任务列表
     */
    PageResponseDTO<MissionVO> getMissionList(MissionQueryDTO queryDTO);
    
    /**
     * 强制关闭任务
     *
     * @param forceCloseDTO 关闭参数
     */
    void forceClose(MissionForceCloseDTO forceCloseDTO);
    
    /**
     * 获取任务详情
     *
     * @param id 任务ID
     * @return 任务详情
     */
    MissionDetailVO getMissionDetail(String id);
}
