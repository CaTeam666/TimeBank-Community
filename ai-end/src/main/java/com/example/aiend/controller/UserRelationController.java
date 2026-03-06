package com.example.aiend.controller;

import com.example.aiend.common.Result;
import com.example.aiend.dto.request.UserRelationAuditDTO;
import com.example.aiend.dto.request.UserRelationQueryDTO;
import com.example.aiend.dto.response.PageResponseDTO;
import com.example.aiend.service.UserRelationService;
import com.example.aiend.vo.UserRelationVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户关系（亲情绑定）控制器
 *
 * @author AI-End
 * @since 2025-12-25
 */
@RestController
@RequestMapping("/user-relations")
@Slf4j
@RequiredArgsConstructor
public class UserRelationController {
    
    private final UserRelationService userRelationService;
    
    /**
     * 获取亲情绑定申请列表
     * 支持分页、状态筛选、关键词搜索
     *
     * @param queryDTO 查询条件
     * @return 分页列表
     */
    @GetMapping
    public Result<PageResponseDTO<UserRelationVO>> getRelationList(UserRelationQueryDTO queryDTO) {
        log.info("收到获取亲情绑定列表请求，查询条件：{}", queryDTO);
        PageResponseDTO<UserRelationVO> result = userRelationService.getRelationList(queryDTO);
        return Result.success(result);
    }
    
    /**
     * 审核亲情绑定申请
     *
     * @param id 关系ID
     * @param auditDTO 审核DTO
     * @return 操作结果
     */
    @PostMapping("/{id}/audit")
    public Result<Void> auditRelation(
            @PathVariable Long id,
            @Valid @RequestBody UserRelationAuditDTO auditDTO) {
        log.info("收到审核亲情绑定申请请求，ID：{}，审核状态：{}", id, auditDTO.getStatus());
        userRelationService.auditRelation(id, auditDTO);
        return Result.success(null, "操作成功");
    }
}
