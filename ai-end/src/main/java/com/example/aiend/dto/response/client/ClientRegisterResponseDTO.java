package com.example.aiend.dto.response.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户端注册响应 DTO
 *
 * @author AI-End
 * @since 2025-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientRegisterResponseDTO {
    
    /**
     * 审核ID
     */
    private String auditId;
    
    /**
     * 初始审核状态 (0:待审核)
     */
    private Integer status;
}
