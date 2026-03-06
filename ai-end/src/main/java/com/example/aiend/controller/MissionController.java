package com.example.aiend.controller;

import com.example.aiend.common.Result;
import com.example.aiend.dto.request.MissionForceCloseDTO;
import com.example.aiend.dto.request.MissionQueryDTO;
import com.example.aiend.dto.response.PageResponseDTO;
import com.example.aiend.service.MissionService;
import com.example.aiend.vo.MissionDetailVO;
import com.example.aiend.vo.MissionVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 任务监控控制器
 *
 * @author AI-End
 * @since 2025-12-26
 */
@RestController
@RequestMapping("/mission")
@Slf4j
@RequiredArgsConstructor
public class MissionController {
    
    private final MissionService missionService;
    
    /**
     * 获取任务列表
     * 支持分页、关键词搜索和多维度筛选
     *
     * @param queryDTO 查询条件
     * @return 分页任务列表
     */
    @GetMapping("/list")
    public Result<PageResponseDTO<MissionVO>> getMissionList(MissionQueryDTO queryDTO) {
        log.info("收到获取任务列表请求，查询条件：{}", queryDTO);
        PageResponseDTO<MissionVO> result = missionService.getMissionList(queryDTO);
        return Result.success(result);
    }
    
    /**
     * 强制关闭任务
     * 管理员强制关闭进行中或待接单的任务，资金退回
     *
     * @param forceCloseDTO 关闭参数
     * @return 操作结果
     */
    @PostMapping("/force-close")
    public Result<Void> forceClose(@Valid @RequestBody MissionForceCloseDTO forceCloseDTO) {
        log.info("收到强制关闭任务请求，参数：{}", forceCloseDTO);
        missionService.forceClose(forceCloseDTO);
        return Result.success(null, "操作成功");
    }
    
    /**
     * 获取任务详情
     *
     * @param id 任务ID
     * @return 任务详情
     */
    @GetMapping("/{id}")
    public Result<MissionDetailVO> getMissionDetail(@PathVariable String id) {
        log.info("收到获取任务详情请求，任务ID：{}", id);
        MissionDetailVO result = missionService.getMissionDetail(id);
        return Result.success(result);
    }
}
