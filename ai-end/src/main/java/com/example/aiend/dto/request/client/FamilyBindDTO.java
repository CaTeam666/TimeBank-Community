package com.example.aiend.dto.request.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 申请绑定亲情账号请求 DTO
 *
 * @author AI-End
 * @since 2026-02-07
 */
@Data
public class FamilyBindDTO {
    
    /**
     * 被绑定长者的手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    /**
     * 关系类型（如:父亲、母亲、爷爷、奶奶）
     */
    @NotBlank(message = "关系类型不能为空")
    private String relation;
    
    /**
     * 证明材料图片URL
     */
    private String proofImg;
}
