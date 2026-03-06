package com.example.aiend.service;

import com.example.aiend.dto.request.BalanceAdjustmentDTO;
import com.example.aiend.dto.request.UserQueryDTO;
import com.example.aiend.dto.request.UserStatusUpdateDTO;
import com.example.aiend.dto.response.PageResponseDTO;
import com.example.aiend.vo.UserVO;

import java.util.Map;

/**
 * 用户服务接口
 *
 * @author AI-End
 * @since 2025-12-21
 */
public interface UserService {
    
    /**
     * 获取用户列表（支持分页、关键词搜索和筛选）
     *
     * @param queryDTO 查询条件
     * @return 分页用户列表
     */
    PageResponseDTO<UserVO> getUserList(UserQueryDTO queryDTO);
    
    /**
     * 获取用户详情
     *
     * @param id 用户ID
     * @return 用户详情
     */
    UserVO getUserDetail(String id);
    
    /**
     * 更新用户状态
     *
     * @param id 用户ID
     * @param updateDTO 状态更新DTO
     */
    void updateUserStatus(String id, UserStatusUpdateDTO updateDTO);
    
    /**
     * 调整用户余额
     *
     * @param id 用户ID
     * @param adjustmentDTO 余额调整DTO
     * @return 包含当前余额的结果
     */
    Map<String, Integer> adjustBalance(String id, BalanceAdjustmentDTO adjustmentDTO);
}
