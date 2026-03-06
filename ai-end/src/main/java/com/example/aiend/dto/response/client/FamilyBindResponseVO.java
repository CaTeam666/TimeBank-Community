package com.example.aiend.dto.response.client;

import lombok.Builder;
import lombok.Data;

/**
 * 绑定申请响应 VO
 *
 * @author AI-End
 * @since 2026-02-07
 */
@Data
@Builder
public class FamilyBindResponseVO {
    
    /**
     * 关系记录ID
     */
    private Long relationId;
}
