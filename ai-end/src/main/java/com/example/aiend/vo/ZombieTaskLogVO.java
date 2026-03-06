package com.example.aiend.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 僵尸任务日志视图对象
 * 用于接口返回的数据展示
 *
 * @author AI-End
 * @since 2026-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZombieTaskLogVO {

    /**
     * 日志ID
     */
    private String id;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 任务标题
     */
    private String taskTitle;

    /**
     * 自动关闭时间
     */
    private String closedTime;

    /**
     * 退款金额
     */
    private Integer refundAmount;

    /**
     * 退款状态 (SUCCESS:成功 FAILURE:失败)
     */
    private String refundStatus;
}
