package com.example.aiend.vo;

import lombok.Data;

/**
 * 任务列表VO
 *
 * @author AI-End
 * @since 2025-12-26
 */
@Data
public class MissionVO {
    
    /**
     * 任务ID
     */
    private String id;
    
    /**
     * 任务标题
     */
    private String title;
    
    /**
     * 任务类型（CHAT, CLEANING, ERRAND, MEDICAL）
     */
    private String type;
    
    /**
     * 任务状态（PENDING, IN_PROGRESS, WAITING_CONFIRM, COMPLETED, CANCELLED, APPEALING）
     */
    private String status;
    
    /**
     * 悬赏金额（时间币）
     */
    private Integer coins;
    
    /**
     * 发布时间
     */
    private String publishTime;
    
    /**
     * 截止时间
     */
    private String deadline;
    
    /**
     * 发布者昵称
     */
    private String creatorName;
    
    /**
     * 发布者真实姓名
     */
    private String creatorRealName;
    
    /**
     * 发布者手机号
     */
    private String creatorPhone;
    
    /**
     * 接单志愿者昵称
     */
    private String volunteerName;
    
    /**
     * 接单志愿者手机号
     */
    private String volunteerPhone;
}
