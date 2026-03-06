package com.example.aiend.controller;

import com.example.aiend.common.Result;
import com.example.aiend.dto.request.IdentityAuditQueryDTO;
import com.example.aiend.dto.request.IdentityAuditResultDTO;
import com.example.aiend.dto.response.PageResponseDTO;
import com.example.aiend.service.IdentityAuditService;
import com.example.aiend.vo.IdentityAuditVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 实名认证审核控制器
 *
 * @author AI-End
 * @since 2025-12-24
 */
@RestController
@RequestMapping("/identity-audits")
@Slf4j
@RequiredArgsConstructor
public class IdentityAuditController {
    
    private final IdentityAuditService identityAuditService;
    
    /**
     * 获取实名审核列表
     * 支持分页、按状态筛选
     *
     * @param queryDTO 查询条件
     * @return 分页审核列表
     */
    @GetMapping
    public Result<PageResponseDTO<IdentityAuditVO>> getAuditList(IdentityAuditQueryDTO queryDTO) {
        log.info("收到获取实名审核列表请求，查询条件：{}", queryDTO);
        PageResponseDTO<IdentityAuditVO> result = identityAuditService.getAuditList(queryDTO);
        return Result.success(result);
    }
    
    /**
     * 获取审核详情
     *
     * @param id 审核任务ID
     * @return 审核详情
     */
    @GetMapping("/{id}")
    public Result<IdentityAuditVO> getAuditDetail(@PathVariable Long id) {
        log.info("收到获取审核详情请求，审核ID：{}", id);
        IdentityAuditVO audit = identityAuditService.getAuditDetail(id);
        return Result.success(audit);
    }
    
    /**
     * 提交审核结果
     *
     * @param id 审核任务ID
     * @param resultDTO 审核结果DTO
     * @return 操作结果
     */
    @PostMapping("/{id}/audit")
    public Result<Void> submitAuditResult(
            @PathVariable Long id,
            @Valid @RequestBody IdentityAuditResultDTO resultDTO) {
        log.info("收到提交审核结果请求，审核ID：{}，审核状态：{}", id, resultDTO.getStatus());
        identityAuditService.submitAuditResult(id, resultDTO);
        return Result.success(null, "审核成功");
    }
}
