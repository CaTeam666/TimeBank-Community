package com.example.aiend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户关系（亲情绑定）审核 DTO
 *
 * @author AI-End
 * @since 2025-12-25
 */
@Data
public class UserRelationAuditDTO {
    
    /**
     * 审核动作（1:通过 2:拒绝）
     * 注意：后端会根据此动作映射到对应的业务状态码 (1:待老人确认, 3:已拒绝)
     */
    @NotNull(message = "审核状态不能为空")
    @Min(value = 1, message = "审核状态只能是1(通过)或2(拒绝)")
    @Max(value = 2, message = "审核状态只能是1(通过)或2(拒绝)")
    private Integer status;
    
    /**
     * 拒绝原因（当 status=2 时必填）
     */
    private String rejectReason;
}
