package com.example.aiend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 仲裁裁决请求DTO
 *
 * @author AI-End
 * @since 2025-12-26
 */
@Data
public class ArbitrationVerdictDTO {
    
    /**
     * 仲裁单ID
     */
    @NotBlank(message = "仲裁单ID不能为空")
    private String id;
    
    /**
     * 裁决类型：REJECT（驳回）, TO_VOLUNTEER（判给志愿者）, TO_PUBLISHER（判给发布者）
     */
    @NotBlank(message = "裁决类型不能为空")
    private String verdictType;
    
    /**
     * 裁决理由/备注
     */
    @NotBlank(message = "裁决理由不能为空")
    private String reason;
}
