package com.example.aiend.dto.response.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 任务分类响应 DTO
 *
 * @author AI-End
 * @since 2025-12-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCategoryDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 分类的英文枚举值
     */
    private String key;
    
    /**
     * 分类的中文显示名称
     */
    private String label;
    
    /**
     * 分类图标名称（可选）
     */
    private String icon;
}
