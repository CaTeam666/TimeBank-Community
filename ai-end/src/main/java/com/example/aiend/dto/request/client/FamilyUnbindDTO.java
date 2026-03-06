package com.example.aiend.dto.request.client;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 解绑亲情账号请求 DTO
 *
 * @author AI-End
 * @since 2026-02-07
 */
@Data
public class FamilyUnbindDTO {
    
    /**
     * 关系记录ID
     */
    @NotNull(message = "关系记录ID不能为空")
    private Long relationId;
}
