package com.example.aiend.controller;

import com.example.aiend.common.Result;
import com.example.aiend.dto.request.RankingLogQueryDTO;
import com.example.aiend.dto.request.RankingLogRetryDTO;
import com.example.aiend.dto.response.PageResponseDTO;
import com.example.aiend.service.RankingService;
import com.example.aiend.vo.RankingLogVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 排名奖励控制器
 *
 * @author AI-End
 * @since 2025-12-27
 */
@RestController
@RequestMapping("/ranking")
@Slf4j
@RequiredArgsConstructor
public class RankingController {
    
    private final RankingService rankingService;
    
    /**
     * 获取奖励发放日志
     * 分页查询奖励发放日志，支持按期数筛选
     *
     * @param queryDTO 查询条件
     * @return 分页日志列表
     */
    @GetMapping("/logs")
    public Result<PageResponseDTO<RankingLogVO>> getRankingLogs(@Valid RankingLogQueryDTO queryDTO) {
        log.info("收到获取奖励发放日志请求，查询条件：{}", queryDTO);
        PageResponseDTO<RankingLogVO> result = rankingService.getRankingLogs(queryDTO);
        return Result.success(result);
    }
    
    /**
     * 手动触发补发
     * 对发放失败的奖励进行补发
     *
     * @param retryDTO 补发请求参数
     * @return 操作结果
     */
    @PostMapping("/retry")
    public Result<Void> retryDistribution(@Valid @RequestBody RankingLogRetryDTO retryDTO) {
        log.info("收到手动触发补发请求，参数：{}", retryDTO);
        rankingService.retryDistribution(retryDTO);
        return Result.success(null, "补发指令已提交");
    }
}
