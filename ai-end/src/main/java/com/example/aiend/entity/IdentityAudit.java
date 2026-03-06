package com.example.aiend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 实名认证审核实体类
 *
 * @author AI-End
 * @since 2025-12-24
 */
@Data
@TableName("sys_identity_audit")
public class IdentityAudit {
    
    /**
     * 审核ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * OCR识别姓名
     */
    @TableField("real_name")
    private String realName;
    
    /**
     * OCR识别身份证号
     */
    @TableField("id_card")
    private String idCard;
    
    /**
     * OCR识别年龄
     */
    @TableField("age")
    private Integer age;
    
    /**
     * 身份证人像面图片URL
     */
    @TableField("id_card_front")
    private String idCardFront;
    
    /**
     * 身份证国徽面图片URL
     */
    @TableField("id_card_back")
    private String idCardBack;
    
    /**
     * 审核状态（0:待审核 1:已通过 2:已驳回）
     */
    @TableField("status")
    private Integer status;
    
    /**
     * 驳回原因
     */
    @TableField("reject_reason")
    private String rejectReason;
    
    /**
     * 审核时间
     */
    @TableField("audit_time")
    private LocalDateTime auditTime;
    
    /**
     * 审核人ID
     */
    @TableField("auditor_id")
    private Long auditorId;
    
    /**
     * 创建/提交时间
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
