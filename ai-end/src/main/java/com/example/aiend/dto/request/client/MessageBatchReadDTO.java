package com.example.aiend.dto.request.client;

import lombok.Data;

import java.util.List;

/**
 * 批量标记消息已读请求DTO
 *
 * @author AI-End
 * @since 2026-02-07
 */
@Data
public class MessageBatchReadDTO {
    
    /**
     * 消息ID列表（与type二选一）
     */
    private List<Long> messageIds;
    
    /**
     * 消息类型（与messageIds二选一，传入则将该类型全部标记已读）
     */
    private String type;
}
