package com.example.aiend.dto.request.client;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * 服务凭证提交请求 DTO
 *
 * @author AI-End
 * @since 2025-12-31
 */
@Data
public class EvidenceSubmitDTO {
    
    /**
     * 任务ID
     */
    @NotBlank(message = "任务ID不能为空")
    @JsonAlias({"task_id", "taskId"})
    private String taskId;
    
    /**
     * 志愿者ID
     */
    @NotBlank(message = "用户ID不能为空")
    @JsonAlias({"user_id", "userId"})
    private String userId;
    
    /**
     * 凭证图片URL（字符串格式）
     */
    private String imageUrl;
    
    /**
     * 凭证图片URL（对象格式，前端可能传 {url: "..."} 结构）
     * 设置时自动提取 url 字段
     */
    @JsonProperty("imageUrl")
    public void setImageUrlFromObject(Object imageUrlObj) {
        if (imageUrlObj instanceof String) {
            this.imageUrl = (String) imageUrlObj;
        } else if (imageUrlObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) imageUrlObj;
            Object url = map.get("url");
            this.imageUrl = url != null ? url.toString() : null;
        }
    }
}
