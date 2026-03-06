package com.example.aiend.dto.response.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务发布响应 DTO
 *
 * @author AI-End
 * @since 2025-12-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskPublishResponseDTO {
    
    /**
     * 新创建的任务ID
     */
    private String taskId;
}
