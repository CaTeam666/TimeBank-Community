package com.example.aiend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 商品状态更新请求DTO
 *
 * @author AI-End Team
 * @since 2024-12-27
 */
@Data
public class ProductStatusDTO {

    /**
     * 商品ID
     */
    @NotBlank(message = "商品ID不能为空")
    private String id;

    /**
     * 状态 (ON_SHELF/OFF_SHELF)
     */
    @NotBlank(message = "状态不能为空")
    private String status;
}
