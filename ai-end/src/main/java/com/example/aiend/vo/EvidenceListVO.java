package com.example.aiend.vo;

import lombok.Data;

/**
 * 存证列表VO
 *
 * @author AI-End Team
 * @since 2024-12-26
 */
@Data
public class EvidenceListVO {

    /**
     * 凭证ID
     */
    private String id;

    /**
     * 关联任务ID
     */
    private String taskId;

    /**
     * 任务标题
     */
    private String taskTitle;

    /**
     * 志愿者ID
     */
    private String volunteerId;

    /**
     * 志愿者姓名
     */
    private String volunteerName;

    /**
     * 凭证图片URL
     */
    private String imageUrl;

    /**
     * 上传时间
     */
    private String createTime;
}
