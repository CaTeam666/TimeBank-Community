package com.example.aiend.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务日志VO
 *
 * @author AI-End
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MissionLogVO {
    
    /**
     * 日志ID
     */
    private String id;
    
    /**
     * 日志时间
     */
    private String time;
    
    /**
     * 日志内容
     */
    private String content;
}
