package com.example.aiend.dto.request.client;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 提交申诉回应请求 DTO
 *
 * @author AI-End
 * @since 2026-01-10
 */
@Data
public class AppealReplyDTO {
    
    /**
     * 任务ID
     */
    @NotBlank(message = "任务ID不能为空")
    @JsonAlias({"task_id", "taskId"})
    private String taskId;
    
    /**
     * 用户ID（回应人）
     */
    @NotBlank(message = "用户ID不能为空")
    @JsonAlias({"user_id", "userId"})
    private String userId;
    
    /**
     * 回应内容
     */
    @NotBlank(message = "回应内容不能为空")
    private String content;
}
