package com.example.aiend.service;

import com.example.aiend.vo.ZombieTaskLogVO;

import java.util.List;

/**
 * 异常数据服务接口
 * 处理系统异常数据，包括僵尸任务管理
 *
 * @author AI-End
 * @since 2026-01-14
 */
public interface AnomalyService {

    /**
     * 获取僵尸任务日志列表
     *
     * @return 僵尸任务日志列表
     */
    List<ZombieTaskLogVO> getZombieTaskLogs();

    /**
     * 手动触发僵尸任务检测
     * 检测并处理异常任务
     */
    void triggerZombieTaskCheck();

    /**
     * 手动重试退款
     *
     * @param logId 日志ID
     * @return 是否成功
     */
    boolean retryRefund(Long logId);
}
