package com.example.aiend.dto.response.client;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 待审核申请列表响应 VO
 *
 * @author AI-End
 * @since 2026-02-07
 */
@Data
@Builder
public class PendingRequestListVO {
    
    /**
     * 待审核申请总数
     */
    private Integer total;
    
    /**
     * 申请列表
     */
    private List<PendingRequestVO> requests;
}
