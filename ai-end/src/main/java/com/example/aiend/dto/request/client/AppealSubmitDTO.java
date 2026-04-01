package com.example.aiend.dto.request.client;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 提交申诉请求 DTO
 *
 * @author AI-End
 * @since 2026-01-03
 */
@Data
public class AppealSubmitDTO {
    
    /**
     * 任务ID
     */
    @NotBlank(message = "任务ID不能为空")
    @JsonAlias({"task_id", "taskId"})
    private String taskId;
    
    /**
     * 申诉发起人ID
     */
    @NotBlank(message = "用户ID不能为空")
    @JsonAlias({"user_id", "userId"})
    private String userId;
    
    /**
     * 申诉理由
     */
    @NotBlank(message = "申诉理由不能为空")
    private String reason;
    
    /**
     * 申诉证据图片URL
     */
    @NotBlank(message = "申诉证据图片不能为空")
    @JsonAlias({"evidence_img", "evidenceImg"})
    private String evidenceImg;
}
