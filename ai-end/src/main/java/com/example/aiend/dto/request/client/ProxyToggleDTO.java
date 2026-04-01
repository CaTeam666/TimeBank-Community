package com.example.aiend.dto.request.client;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 切换代理模式请求 DTO
 *
 * @author AI-End
 * @since 2026-02-07
 */
@Data
public class ProxyToggleDTO {
    
    /**
     * 长者用户ID（为null时退出代理模式，使用String避免JS大整数精度丢失）
     */
    private String parentId;
    
    /**
     * 是否开启代理模式（true:开启 false:关闭）
     */
    @NotNull(message = "enable参数不能为空")
    private Boolean enable;
}
