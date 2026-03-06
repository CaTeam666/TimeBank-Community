package com.example.aiend.controller;

import com.example.aiend.common.Result;
import com.example.aiend.dto.request.BalanceAdjustmentDTO;
import com.example.aiend.dto.request.UserQueryDTO;
import com.example.aiend.dto.request.UserStatusUpdateDTO;
import com.example.aiend.dto.response.PageResponseDTO;
import com.example.aiend.service.UserService;
import com.example.aiend.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户管理控制器
 *
 * @author AI-End
 * @since 2025-12-21
 */
@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    /**
     * 获取用户列表
     * 支持分页、关键词搜索和筛选
     *
     * @param queryDTO 查询条件
     * @return 分页用户列表
     */
    @GetMapping
    public Result<PageResponseDTO<UserVO>> getUserList(UserQueryDTO queryDTO) {
        log.info("收到获取用户列表请求，查询条件：{}", queryDTO);
        PageResponseDTO<UserVO> result = userService.getUserList(queryDTO);
        return Result.success(result);
    }
    
    /**
     * 获取用户详情
     *
     * @param id 用户ID
     * @return 用户详情
     */
    @GetMapping("/{id}")
    public Result<UserVO> getUserDetail(@PathVariable String id) {
        log.info("收到获取用户详情请求，用户ID：{}", id);
        UserVO user = userService.getUserDetail(id);
        return Result.success(user);
    }
    
    /**
     * 更新用户状态（冻结/解冻）
     *
     * @param id 用户ID
     * @param updateDTO 状态更新DTO
     * @return 操作结果
     */
    @PatchMapping("/{id}/status")
    public Result<Void> updateUserStatus(
            @PathVariable String id,
            @Valid @RequestBody UserStatusUpdateDTO updateDTO) {
        log.info("收到更新用户状态请求，用户ID：{}，目标状态：{}", id, updateDTO.getStatus());
        userService.updateUserStatus(id, updateDTO);
        return Result.success(null, "状态更新成功");
    }
    
    /**
     * 调整用户余额
     *
     * @param id 用户ID
     * @param adjustmentDTO 余额调整DTO
     * @return 包含当前余额的结果
     */
    @PostMapping("/{id}/balance-adjustment")
    public Result<Map<String, Integer>> adjustBalance(
            @PathVariable String id,
            @Valid @RequestBody BalanceAdjustmentDTO adjustmentDTO) {
        log.info("收到调整用户余额请求，用户ID：{}，调整金额：{}", id, adjustmentDTO.getAmount());
        Map<String, Integer> result = userService.adjustBalance(id, adjustmentDTO);
        return Result.success(result, "余额调整成功");
    }
}
