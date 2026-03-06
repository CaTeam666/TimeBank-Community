package com.example.aiend.controller;

import com.example.aiend.common.Result;
import com.example.aiend.service.AnomalyService;
import com.example.aiend.vo.ZombieTaskLogVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 异常数据控制器
 * 处理系统异常数据相关接口，包括僵尸任务管理
 *
 * @author AI-End
 * @since 2026-01-14
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/anomaly")
public class AnomalyController {

    private final AnomalyService anomalyService;

    /**
     * 获取僵尸任务日志列表
     * 返回被系统自动关闭的僵尸任务记录
     *
     * @return 僵尸任务日志列表
     */
    @GetMapping("/zombie/logs")
    public Result<List<ZombieTaskLogVO>> getZombieTaskLogs() {
        log.info("获取僵尸任务日志列表");
        List<ZombieTaskLogVO> logs = anomalyService.getZombieTaskLogs();
        return Result.success(logs);
    }

    /**
     * 手动触发僵尸任务检测
     * 用于管理员手动执行检测
     *
     * @return 触发结果
     */
    @PostMapping("/zombie/check")
    public Result<Void> triggerZombieTaskCheck() {
        log.info("手动触发僵尸任务检测");
        anomalyService.triggerZombieTaskCheck();
        return Result.success();
    }

    /**
     * 手动重试退款
     * 用于处理退款失败的僵尸任务
     *
     * @param logId 日志ID
     * @return 重试结果
     */
    @PostMapping("/zombie/retry/{logId}")
    public Result<Boolean> retryRefund(@PathVariable Long logId) {
        log.info("手动重试退款，日志ID：{}", logId);
        boolean success = anomalyService.retryRefund(logId);
        return Result.success(success);
    }
}
