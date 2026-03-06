package com.example.aiend.dto.response.client;

import lombok.Builder;
import lombok.Data;

/**
 * 代理模式切换响应 VO
 *
 * @author AI-End
 * @since 2026-02-07
 */
@Data
@Builder
public class ProxyToggleResponseVO {
    
    /**
     * 代理令牌（后续请求使用此token代表长者身份）
     */
    private String proxyToken;
}
