package com.example.aiend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 实名审核结果 DTO
 *
 * @author AI-End
 * @since 2025-12-24
 */
@Data
public class IdentityAuditResultDTO {
    
    /**
     * 审核结果状态 (APPROVED, REJECTED)
     */
    @NotBlank(message = "审核状态不能为空")
    @Pattern(regexp = "^(APPROVED|REJECTED)$", message = "审核状态只能是 APPROVED 或 REJECTED")
    private String status;
    
    /**
     * 驳回原因 (当 status 为 REJECTED 时必填)
     */
    private String rejectReason;
}
