package com.example.aiend.service.client;

import com.example.aiend.dto.response.client.FileUploadResponseDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务接口
 *
 * @author AI-End
 * @since 2025-12-28
 */
public interface FileUploadService {
    
    /**
     * 上传文件
     *
     * @param file 文件对象
     * @return 文件上传响应（包含文件访问URL）
     */
    FileUploadResponseDTO uploadFile(MultipartFile file);
}
