package com.example.aiend.service.client.impl;

import com.aliyun.oss.OSS;
import com.example.aiend.common.exception.BusinessException;
import com.example.aiend.dto.response.client.FileUploadResponseDTO;
import com.example.aiend.service.client.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件上传服务实现类
 *
 * @author AI-End
 * @since 2025-12-28
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {

    private final OSS ossClient;

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    @Value("${file.upload.url-prefix:http://localhost:8080/uploads}")
    private String fileUrlPrefix;

    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;

    @Value("${aliyun.oss.url-prefix}")
    private String ossUrlPrefix;
    
    /**
     * 允许的文件类型
     */
    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "bmp"};
    
    /**
     * 最大文件大小（5MB）
     */
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    
    /**
     * 上传文件至阿里云 OSS
     *
     * @param file 文件对象
     * @return 文件上传响应（包含文件访问URL）
     */
    @Override
    public FileUploadResponseDTO uploadFile(MultipartFile file) {
        // 默认上传到本地
        return uploadFile(file, "local");
    }

    @Override
    public FileUploadResponseDTO uploadFile(MultipartFile file, String target) {
        // 验证文件
        validateFile(file);

        if ("oss".equalsIgnoreCase(target)) {
            return uploadToOss(file);
        } else {
            return uploadToLocal(file);
        }
    }

    /**
     * 上传文件到阿里云 OSS
     */
    private FileUploadResponseDTO uploadToOss(MultipartFile file) {
        if (ossClient == null || bucketName.isEmpty() || ossUrlPrefix.isEmpty()) {
            throw new BusinessException(500, "OSS 服务未配置或不可用");
        }
        log.info("开始上传文件到阿里云 OSS，文件名：{}", file.getOriginalFilename());
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String newFilename = UUID.randomUUID().toString().replace("-", "") + "." + extension;
            String ossKey = datePath + "/" + newFilename;

            ossClient.putObject(bucketName, ossKey, file.getInputStream());

            String finalUrl = ossUrlPrefix;
            if (!finalUrl.endsWith("/")) {
                finalUrl += "/";
            }
            finalUrl += ossKey;

            log.info("文件上传 OSS 成功，URL：{}", finalUrl);
            return FileUploadResponseDTO.builder().url(finalUrl).build();
        } catch (IOException e) {
            log.error("文件上传 OSS 失败", e);
            throw new BusinessException(500, "文件上传失败 (OSS): " + e.getMessage());
        }
    }

    /**
     * 上传文件到本地磁盘
     */
    private FileUploadResponseDTO uploadToLocal(MultipartFile file) {
        log.info("开始上传文件到本地磁盘，文件名：{}", file.getOriginalFilename());

        String originalFilename = file.getOriginalFilename();
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String extension = getFileExtension(originalFilename);
        String newFilename = UUID.randomUUID().toString().replace("-", "") + "." + extension;

        // 拼接物理存储路径
        File baseDir = new File(uploadPath).getAbsoluteFile();
        File targetDir = new File(baseDir, datePath);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        File destFile = new File(targetDir, newFilename);

        try {
            file.transferTo(destFile.getAbsoluteFile());
            log.info("文件上传本地成功，绝对路径：{}", destFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("文件上传本地失败", e);
            throw new BusinessException(500, "文件上传失败 (LOCAL): " + e.getMessage());
        }

        // 构建访问 URL
        String finalUrl = fileUrlPrefix;
        if (!finalUrl.endsWith("/")) {
            finalUrl += "/";
        }
        finalUrl += datePath + "/" + newFilename;

        return FileUploadResponseDTO.builder().url(finalUrl).build();
    }

    /**
     * 验证文件
     *
     * @param file 文件对象
     */
    private void validateFile(MultipartFile file) {
        // 检查文件是否为空
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请选择要上传的文件");
        }
        
        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(400, "文件大小不能超过5MB");
        }
        
        // 检查文件类型
        String extension = getFileExtension(file.getOriginalFilename());
        boolean isAllowed = false;
        for (String allowedExt : ALLOWED_EXTENSIONS) {
            if (allowedExt.equalsIgnoreCase(extension)) {
                isAllowed = true;
                break;
            }
        }
        
        if (!isAllowed) {
            throw new BusinessException(400, "只支持 jpg、jpeg、png、gif、bmp 格式的图片");
        }
    }
    
    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
