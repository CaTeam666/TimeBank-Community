package com.example.aiend.dto.request.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 审核绑定申请请求 DTO
 *
 * @author AI-End
 * @since 2026-02-07
 */
@Data
public class FamilyReviewDTO {
    
    /**
     * 关系记录ID
     */
    @NotNull(message = "关系记录ID不能为空")
    private Long relationId;
    
    /**
     * 审核动作: "approve"(通过) 或 "reject"(拒绝)
     */
    @NotBlank(message = "审核动作不能为空")
    @Pattern(regexp = "^(approve|reject)$", message = "审核动作只能是approve或reject")
    private String action;
    
    /**
     * 拒绝原因（action为reject时必填）
     */
    private String rejectReason;
}
