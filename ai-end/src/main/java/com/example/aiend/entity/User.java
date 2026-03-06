package com.example.aiend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 *
 * @author AI-End
 * @since 2025-12-19
 */
@Data
@TableName("sys_user")
public class User {
    
    /**
     * 用户ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 真实姓名
     */
    @TableField("realName")
    private String realName;
    
    /**
     * 密码
     */
    @TableField("password")
    private String password;
    
    /**
     * 身份证号
     */
    @TableField("id_card")
    private String idCard;
    
    /**
     * 昵称
     */
    @TableField("nickname")
    private String nickname;
    
    /**
     * 角色（ADMIN, VOLUNTEER, ELDER, CHILD_AGENT）
     */
    @TableField("role")
    private String role;
    
    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;
    
    /**
     * 邮箱
     */
    @TableField("email")
    private String email;
    
    /**
     * 头像
     */
    @TableField("avatar")
    private String avatar;
    
    /**
     * 时间币余额
     */
    @TableField("balance")
    private Integer balance;
    
    /**
     * 冻结时间币（发布任务时冻结，验收后转给志愿者）
     */
    @TableField("frozen_balance")
    private Integer frozenBalance;
    
    /**
     * 状态（0:禁用 1:启用）
     */
    @TableField("status")
    private Integer status;
    
    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
    
    /**
     * 注册时间
     */
    @TableField("registerTime")
    private LocalDateTime registerTime;
    
    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除标记（0:未删除 1:已删除）
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
