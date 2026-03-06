package com.example.aiend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 任务强制关闭DTO
 *
 * @author AI-End
 * @since 2025-12-26
 */
@Data
public class MissionForceCloseDTO {
    
    /**
     * 任务ID
     */
    @NotBlank(message = "任务ID不能为空")
    private String taskId;
    
    /**
     * 关闭原因
     */
    @NotBlank(message = "关闭原因不能为空")
    private String reason;
}
