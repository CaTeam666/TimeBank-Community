package com.example.aiend.dto.request.client;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 志愿者签到请求 DTO
 *
 * @author AI-End
 * @since 2025-12-31
 */
@Data
public class CheckInDTO {
    
    /**
     * 任务ID
     */
    @NotBlank(message = "任务ID不能为空")
    @JsonAlias({"task_id", "taskId"})
    private String taskId;
    
    /**
     * 志愿者ID
     */
    @NotBlank(message = "用户ID不能为空")
    @JsonAlias({"user_id", "userId"})
    private String userId;
    
    /**
     * 签到信息（如位置、备注等）
     */
    @JsonAlias({"check_in_info", "checkInInfo"})
    private String checkInInfo;
}
