package com.example.aiend.vo;

import lombok.Data;

import java.util.List;

/**
 * 任务详情VO
 *
 * @author AI-End
 * @since 2025-12-26
 */
@Data
public class MissionDetailVO {
    
    /**
     * 任务ID
     */
    private String id;
    
    /**
     * 任务标题
     */
    private String title;
    
    /**
     * 任务描述
     */
    private String description;
    
    /**
     * 任务类型
     */
    private String type;
    
    /**
     * 任务状态
     */
    private String status;
    
    /**
     * 悬赏金额（时间币）
     */
    private Integer coins;
    
    /**
     * 服务地址
     */
    private String address;
    
    /**
     * 发布时间
     */
    private String publishTime;
    
    /**
     * 截止时间
     */
    private String deadline;
    
    /**
     * 发布者ID
     */
    private String creatorId;
    
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
     * 发布者头像
     */
    private String creatorAvatar;
    
    /**
     * 发布者信用分
     */
    private Integer creatorCredit;
    
    /**
     * 志愿者ID
     */
    private String volunteerId;
    
    /**
     * 志愿者昵称
     */
    private String volunteerName;
    
    /**
     * 志愿者手机号
     */
    private String volunteerPhone;
    
    /**
     * 志愿者头像
     */
    private String volunteerAvatar;
    
    /**
     * 志愿者信用分
     */
    private Integer volunteerCredit;
    
    /**
     * 任务日志列表
     */
    private List<MissionLogVO> logs;
}
