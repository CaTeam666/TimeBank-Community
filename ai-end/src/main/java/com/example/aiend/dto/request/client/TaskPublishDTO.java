package com.example.aiend.dto.request.client;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 任务发布请求 DTO
 *
 * @author AI-End
 * @since 2025-12-29
 */
@Data
public class TaskPublishDTO {
    
    /**
     * 任务标题
     */
    @NotBlank(message = "任务标题不能为空")
    private String title;
    
    /**
     * 服务类型
     * 支持英文: CHAT, CLEANING, ERRAND, MEDICAL, OTHER
     * 支持中文: 聊天陪伴, 家务清洁, 代买跑腿, 就医陪护, 其他服务
     */
    @NotBlank(message = "服务类型不能为空")
    private String type;
    
    /**
     * 任务详细描述
     */
    @NotBlank(message = "任务描述不能为空")
    private String description;
    
    /**
     * 悬赏时间币
     */
    @NotNull(message = "悬赏时间币不能为空")
    @Min(value = 1, message = "悬赏时间币必须大于0")
    private Integer coins;
    
    /**
     * 详细地址
     */
    @NotBlank(message = "详细地址不能为空")
    private String location;
    
    /**
     * 服务日期 (YYYY-MM-DD)
     */
    @NotBlank(message = "服务日期不能为空")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "服务日期格式必须为YYYY-MM-DD")
    private String date;
    
    /**
     * 服务时间段 (如 09:00 - 10:00)
     */
    @NotBlank(message = "服务时间段不能为空")
    private String timeRange;
    
    /**
     * 发布者ID（可选）
     * 用于指定发布人，如果不传则通过Token自动解析
     */
    private Long publisherId;
}
