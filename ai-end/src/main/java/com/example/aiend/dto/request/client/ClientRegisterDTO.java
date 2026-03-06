package com.example.aiend.dto.request.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户端注册请求 DTO
 *
 * @author AI-End
 * @since 2025-12-28
 */
@Data
public class ClientRegisterDTO {
    
    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20位之间")
    private String password;
    
    /**
     * 昵称
     */
    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;
    
    /**
     * 真实姓名
     */
    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;
    
    /**
     * 身份证号
     */
    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$", 
             message = "身份证号格式不正确")
    private String idCard;
    
    /**
     * 身份证人像面图片URL
     */
    @NotBlank(message = "身份证人像面图片不能为空")
    private String idCardFront;
    
    /**
     * 身份证国徽面图片URL
     */
    @NotBlank(message = "身份证国徽面图片不能为空")
    private String idCardBack;
    
    /**
     * 用户角色: SENIOR(老人), VOLUNTEER(志愿者), AGENT(子女代理人)
     */
    @NotBlank(message = "用户角色不能为空")
    @Pattern(regexp = "^(SENIOR|VOLUNTEER|AGENT)$", message = "角色必须是SENIOR、VOLUNTEER或AGENT")
    private String role;
}
