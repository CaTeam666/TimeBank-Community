package com.example.aiend.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息列表VO
 * 用于展示用户的待办消息
 *
 * @author AI-End
 * @since 2026-02-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageVO {
    
    /**
     * 消息ID
     */
    private Long id;
    
    /**
     * 接收人ID
     */
    private Long receiverId;
    
    /**
     * 消息类型（FAMILY_BIND:亲情绑定, TASK_VERIFY:任务验收）
     */
    private String type;
    
    /**
     * 消息类型名称
     */
    private String typeName;
    
    /**
     * 业务ID（关联的具体数据主键）
     */
    private Long bizId;
    
    /**
     * 消息标题
     */
    private String title;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 消息状态（0:待处理 1:已处理）
     */
    private Integer status;
    
    /**
     * 前端跳转路由
     */
    private String route;
    
    /**
     * 创建时间
     */
    private String createTime;
}
