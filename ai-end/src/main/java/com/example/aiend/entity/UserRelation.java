package com.example.aiend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户关系实体类（亲情绑定）
 *
 * @author AI-End
 * @since 2025-12-25
 */
@Data
@TableName("sys_user_relation")
public class UserRelation {
    
    /**
     * 关系ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 子女/代理人用户ID
     */
    @TableField("child_id")
    private Long childId;
    
    /**
     * 父母/老人用户ID
     */
    @TableField("parent_id")
    private Long parentId;
    
    /**
     * 关系类型（如:父亲、母亲、爷爷、奶奶）
     */
    @TableField("relation")
    private String relation;
    
    /**
     * 关系状态（0:待管理员审核 1:待老人确认 2:已绑定 3:已拒绝）
     */
    @TableField("status")
    private Integer status;
    
    /**
     * 证明材料图片URL（户口本/合照等）
     */
    @TableField("proof_img")
    private String proofImg;
    
    /**
     * 审核拒绝原因
     */
    @TableField("reject_reason")
    private String rejectReason;
    
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
