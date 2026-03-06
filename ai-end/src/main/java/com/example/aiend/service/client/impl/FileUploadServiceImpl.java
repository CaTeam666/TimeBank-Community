package com.example.aiend.service.client.impl;

import com.example.aiend.common.exception.BusinessException;
import com.example.aiend.dto.response.client.FileUploadResponseDTO;
import com.example.aiend.service.client.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
public class FileUploadServiceImpl implements FileUploadService {
    
    /**
     * 文件上传根目录
     */
    @Value("${file.upload.path:uploads}")
    private String uploadPath;
    
    /**
     * 文件访问URL前缀
     */
    @Value("${file.upload.url-prefix:http://localhost:8080/uploads}")
    private String urlPrefix;
    
    /**
     * 允许的文件类型
     */
    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "bmp"};
    
    /**
     * 最大文件大小（5MB）
     */
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    
    /**
     * 上传文件
     *
     * @param file 文件对象
     * @return 文件上传响应（包含文件访问URL）
     */
    @Override
    public FileUploadResponseDTO uploadFile(MultipartFile file) {
        log.info("开始上传文件，原始文件名：{}", file.getOriginalFilename());
        
        // 验证文件
        validateFile(file);
        
        // 生成存储路径（按日期分目录，使用绝对路径）
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        // 获取项目根目录的绝对路径
        Path basePath = Paths.get(uploadPath).toAbsolutePath();
        Path directoryPath = basePath.resolve(datePath);
        
        // 创建目录
        try {
            Files.createDirectories(directoryPath);
            log.info("上传目录：{}", directoryPath);
        } catch (IOException e) {
            log.error("创建上传目录失败：{}", directoryPath, e);
            throw new BusinessException(500, "文件上传失败");
        }
        
        // 生成新文件名（UUID + 原始扩展名）
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String newFilename = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        
        // 保存文件（使用 Files.copy 从输入流复制，避免相对路径问题）
        Path filePath = directoryPath.resolve(newFilename);
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            log.info("文件保存成功，路径：{}", filePath);
        } catch (IOException e) {
            log.error("文件保存失败", e);
            throw new BusinessException(500, "文件上传失败");
        }
        
        // 构建访问URL
        String fileUrl = urlPrefix + "/" + datePath.replace(File.separator, "/") + "/" + newFilename;
        log.info("文件上传成功，访问URL：{}", fileUrl);
        
        return FileUploadResponseDTO.builder()
                .url(fileUrl)
                .build();
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
