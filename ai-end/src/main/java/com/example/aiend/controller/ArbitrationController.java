package com.example.aiend.controller;

import com.example.aiend.common.Result;
import com.example.aiend.dto.request.ArbitrationQueryDTO;
import com.example.aiend.dto.request.ArbitrationVerdictDTO;
import com.example.aiend.dto.response.PageResponseDTO;
import com.example.aiend.service.ArbitrationService;
import com.example.aiend.vo.ArbitrationDetailVO;
import com.example.aiend.vo.ArbitrationListVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 仲裁控制器
 *
 * @author AI-End
 * @since 2025-12-26
 */
@RestController
@RequestMapping("/arbitration")
@Slf4j
@RequiredArgsConstructor
public class ArbitrationController {
    
    private final ArbitrationService arbitrationService;
    
    /**
     * 获取仲裁列表
     * 分页获取仲裁案件列表，支持状态筛选
     *
     * @param queryDTO 查询条件
     * @return 分页仲裁列表
     */
    @GetMapping("/list")
    public Result<PageResponseDTO<ArbitrationListVO>> getArbitrationList(ArbitrationQueryDTO queryDTO) {
        log.info("收到获取仲裁列表请求，查询条件：{}", queryDTO);
        PageResponseDTO<ArbitrationListVO> result = arbitrationService.getArbitrationList(queryDTO);
        return Result.success(result);
    }
    
    /**
     * 获取仲裁详情
     * 获取单条仲裁案件的完整详情，包含关联的任务信息和证据
     *
     * @param id 仲裁单ID
     * @return 仲裁详情
     */
    @GetMapping("/{id}")
    public Result<ArbitrationDetailVO> getArbitrationDetail(@PathVariable String id) {
        log.info("收到获取仲裁详情请求，仲裁单ID：{}", id);
        ArbitrationDetailVO result = arbitrationService.getArbitrationDetail(id);
        return Result.success(result);
    }
    
    /**
     * 提交裁决
     * 管理员提交裁决结果
     *
     * @param verdictDTO 裁决请求参数
     * @param handlerId 处理人ID（从请求头获取）
     * @return 操作结果
     */
    @PostMapping("/verdict")
    public Result<Void> submitVerdict(
            @Valid @RequestBody ArbitrationVerdictDTO verdictDTO,
            @RequestHeader(value = "X-User-Id", required = false) Long handlerId) {
        log.info("收到提交裁决请求，参数：{}，处理人ID：{}", verdictDTO, handlerId);
        arbitrationService.submitVerdict(verdictDTO, handlerId);
        return Result.success(null, "操作成功");
    }
}
