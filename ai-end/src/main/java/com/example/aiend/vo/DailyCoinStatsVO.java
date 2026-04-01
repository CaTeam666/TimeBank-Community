package com.example.aiend.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * 每日时间币统计视图对象
 *
 * @author AI-End
 * @since 2026-03-22
 */
@Data
@Builder
public class DailyCoinStatsVO {

    /**
     * 统计日期
     */
    private LocalDate statDate;

    /**
     * 任务收入总额(type=1)
     */
    private Integer income;

    /**
     * 任务支出总额(type=2)
     */
    private Integer expense;

    /**
     * 兑换支出总额(type=3)
     */
    private Integer exchange;

    /**
     * 系统调整总额(type=4)
     */
    private Integer systemAdjust;

    /**
     * 交易笔数
     */
    private Integer txCount;
}
