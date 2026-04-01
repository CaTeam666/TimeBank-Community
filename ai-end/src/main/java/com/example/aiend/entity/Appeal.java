package com.example.aiend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 申诉实体类
 *
 * @author AI-End
 * @since 2025-12-26
 */
@Data
@TableName("tb_appeal")
public class Appeal {
    
    /**
     * 申诉ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 任务ID
     */
    @TableField("task_id")
    private Long taskId;
    
    /**
     * 申诉发起人ID
     */
    @TableField("proposer_id")
    private Long proposerId;
    
    /**
     * 申诉类型（如：拒不验收、虚假服务）
     */
    @TableField("type")
    private String type;
    
    /**
     * 申诉理由
     */
    @TableField("reason")
    private String reason;
    
    /**
     * 被申诉人回应
     */
    @TableField("defendant_response")
    private String defendantResponse;
    
    /**
     * 被申诉人回应时间
     */
    @TableField("response_time")
    private LocalDateTime responseTime;

    /**
     * 被申诉人证据图片URL
     */
    @TableField("defendant_evidence_img")
    private String defendantEvidenceImg;
    
    /**
     * 证据图片URL（JSON数组或逗号分隔）
     */
    @TableField("evidence_img")
    private String evidenceImg;
    
    /**
     * 裁决结果 (REJECT, TO_VOLUNTEER, TO_PUBLISHER)
     */
    @TableField("handling_result")
    private String handlingResult;
    
    /**
     * 裁决理由/备注
     */
    @TableField("handling_reason")
    private String handlingReason;
    
    /**
     * 处理人ID
     */
    @TableField("handler_id")
    private Long handlerId;
    
    /**
     * 处理时间
     */
    @TableField("handle_time")
    private LocalDateTime handleTime;
    
    /**
     * 申诉状态（0:待处理 1:已通过 2:已驳回）
     */
    @TableField("status")
    private Integer status;
    
    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除标记（0:未删除 1:已删除）
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
