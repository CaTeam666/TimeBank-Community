package com.example.aiend.dto.request.client;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 标记消息已读请求DTO
 *
 * @author AI-End
 * @since 2026-02-07
 */
@Data
public class MessageReadDTO {
    
    /**
     * 消息ID（单条标记时使用）
     */
    @NotNull(message = "消息ID不能为空")
    private Long messageId;
}
