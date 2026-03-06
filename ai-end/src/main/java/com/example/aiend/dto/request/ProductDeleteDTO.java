package com.example.aiend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 商品删除请求DTO
 *
 * @author AI-End Team
 * @since 2024-12-27
 */
@Data
public class ProductDeleteDTO {

    /**
     * 商品ID
     */
    @NotBlank(message = "商品ID不能为空")
    private String id;
}
