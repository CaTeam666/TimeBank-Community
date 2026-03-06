package com.example.aiend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户状态更新 DTO
 *
 * @author AI-End
 * @since 2025-12-21
 */
@Data
public class UserStatusUpdateDTO {
    
    /**
     * 目标状态 (NORMAL 或 FROZEN)
     */
    @NotBlank(message = "状态不能为空")
    private String status;
}
