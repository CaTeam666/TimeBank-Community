package com.example.aiend.controller.client;

import com.example.aiend.common.Result;
import com.example.aiend.dto.request.client.ClientLoginRequestDTO;
import com.example.aiend.dto.request.client.ClientRegisterDTO;
import com.example.aiend.dto.response.client.ClientAuditResultDTO;
import com.example.aiend.dto.response.client.ClientLoginResponseDTO;
import com.example.aiend.dto.response.client.ClientRegisterResponseDTO;
import com.example.aiend.dto.response.client.FileUploadResponseDTO;
import com.example.aiend.service.client.ClientAuthService;
import com.example.aiend.service.client.FileUploadService;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端认证控制器
 * 处理用户端的登录、注册、审核查询等认证相关请求
 *
 * @author AI-End
 * @since 2025-12-28
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@Validated
@RequestMapping("/sys")
public class ClientAuthController {
    
    private final ClientAuthService clientAuthService;
    private final FileUploadService fileUploadService;
    
    /**
     * 用户登录
     *
     * @param loginRequest 登录请求（手机号+密码）
     * @return 登录响应（包含 token 和用户信息）
     */
    @PostMapping("/login")
    public Result<ClientLoginResponseDTO> login(@Valid @RequestBody ClientLoginRequestDTO loginRequest) {
        log.info("用户端登录请求，手机号：{}", loginRequest.getPhone());
        ClientLoginResponseDTO response = clientAuthService.login(loginRequest);
        return Result.success(response, "登录成功");
    }
    
    /**
     * 用户注册
     * 提交实名认证信息，等待审核
     *
     * @param registerRequest 注册请求（包含手机号、密码、实名信息、身份证照片等）
     * @return 注册响应（包含审核ID和初始状态）
     */
    @PostMapping("/register")
    public Result<ClientRegisterResponseDTO> register(@Valid @RequestBody ClientRegisterDTO registerRequest) {
        log.info("用户端注册请求，手机号：{}", registerRequest.getPhone());
        ClientRegisterResponseDTO response = clientAuthService.register(registerRequest);
        return Result.success(response, "提交成功，请等待审核");
    }
    
    /**
     * 审核状态查询
     * 轮询查询注册审核结果
     *
     * @param auditId 审核ID
     * @return 审核结果（包含状态、驳回原因、token、用户信息）
     */
    @GetMapping("/audit/result")
    public Result<ClientAuditResultDTO> getAuditResult(
            @RequestParam @NotBlank(message = "审核ID不能为空") String auditId) {
        log.info("查询审核结果，auditId：{}", auditId);
        ClientAuditResultDTO response = clientAuthService.getAuditResult(auditId);
        return Result.success(response);
    }
    
    /**
     * 文件上传 (支持指定目标)
     * 用于上传服务凭证等图片文件
     *
     * @param file 文件对象
     * @param target 存储目标 (local 或 oss)
     * @return 文件访问URL
     */
    @PostMapping({"/upload", "/file/upload"})
    public Result<FileUploadResponseDTO> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "target", required = false) String target) {
        log.info("文件上传请求，文件名：{}，目标：{}", file.getOriginalFilename(), target);
        FileUploadResponseDTO response = fileUploadService.uploadFile(file, target);
        return Result.success(response);
    }

    /**
     * OCR 专用文件上传 (强制上传至 OSS)
     * 解决云端 OCR 服务无法访问本地 localhost 路径的问题
     */
    @PostMapping("/upload/ocr")
    public Result<FileUploadResponseDTO> uploadFileForOcr(@RequestParam("file") MultipartFile file) {
        log.info("OCR 专用上传请求，文件名：{}", file.getOriginalFilename());
        FileUploadResponseDTO response = fileUploadService.uploadFile(file, "oss");
        return Result.success(response);
    }
}
