package com.example.aiend.dto.request.client;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 任务接单请求DTO
 *
 * @author AI-End
 * @since 2025-12-31
 */
@Data
public class TaskAcceptDTO {
    
    /**
     * 任务ID
     */
    @NotBlank(message = "任务ID不能为空")
    private String taskId;
    
    /**
     * 志愿者ID（当前登录用户）
     */
    @NotBlank(message = "用户ID不能为空")
    private String userId;
}
