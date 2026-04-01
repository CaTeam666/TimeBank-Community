package com.example.aiend.vo;

import lombok.Data;

/**
 * 排行榜项视图对象
 * 用于展示单个排行项信息
 *
 * @author AI-End
 * @since 2026-02-27
 */
@Data
public class RankingItemVO {
    
    /**
     * 排名
     */
    private Integer rank;
    
    /**
     * 志愿者ID
     */
    private String volunteerId;
    
    /**
     * 志愿者姓名
     */
    private String volunteerName;
    
    /**
     * 志愿者头像
     */
    private String volunteerAvatar;
    
    /**
     * 接单数
     */
    private Integer orderCount;
}
