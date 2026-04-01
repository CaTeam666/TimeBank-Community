package com.example.aiend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日时间币统计实体类
 *
 * @author AI-End
 * @since 2026-03-22
 */
@Data
@TableName("tb_daily_coin_stats")
public class DailyCoinStats {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 统计日期
     */
    @TableField("stat_date")
    private LocalDate statDate;

    /**
     * 任务收入总额(type=1)
     */
    @TableField("income")
    private Integer income;

    /**
     * 任务支出总额(type=2，取绝对值)
     */
    @TableField("expense")
    private Integer expense;

    /**
     * 兑换支出总额(type=3，取绝对值)
     */
    @TableField("exchange")
    private Integer exchange;

    /**
     * 系统调整总额(type=4)
     */
    @TableField("system_adjust")
    private Integer systemAdjust;

    /**
     * 交易笔数
     */
    @TableField("tx_count")
    private Integer txCount;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
}
