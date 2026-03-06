package com.example.aiend.dto.request.client;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 提交志愿者评价请求 DTO
 *
 * @author AI-End
 * @since 2026-01-02
 */
@Data
public class ReviewSubmitDTO {
    
    /**
     * 任务ID
     */
    @NotBlank(message = "任务ID不能为空")
    @JsonAlias({"task_id", "taskId"})
    private String taskId;
    
    /**
     * 发布者ID（评价人）
     */
    @NotBlank(message = "发布者ID不能为空")
    @JsonAlias({"publisher_id", "publisherId"})
    private String publisherId;
    
    /**
     * 志愿者ID（被评价人）
     */
    @NotBlank(message = "志愿者ID不能为空")
    @JsonAlias({"volunteer_id", "volunteerId"})
    private String volunteerId;
    
    /**
     * 评分 (1-5)
     */
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    private Integer rating;
    
    /**
     * 评价内容（可选）
     */
    private String content;
}
