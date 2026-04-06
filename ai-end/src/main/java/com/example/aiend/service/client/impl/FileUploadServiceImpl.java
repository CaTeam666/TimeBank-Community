package com.example.aiend.service.client.impl;

import com.aliyun.oss.OSS;
import com.example.aiend.common.exception.BusinessException;
import com.example.aiend.dto.response.client.FileUploadResponseDTO;
import com.example.aiend.service.client.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {

    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "bmp"};
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private final ObjectProvider<OSS> ossClientProvider;

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    @Value("${file.upload.url-prefix:http://localhost:8080/uploads}")
    private String fileUrlPrefix;

    @Value("${aliyun.oss.bucket-name:}")
    private String bucketName;

    @Value("${aliyun.oss.url-prefix:}")
    private String ossUrlPrefix;

    @Override
    public FileUploadResponseDTO uploadFile(MultipartFile file) {
        return uploadFile(file, "local");
    }

    @Override
    public FileUploadResponseDTO uploadFile(MultipartFile file, String target) {
        validateFile(file);
        if ("oss".equalsIgnoreCase(target)) {
            return uploadToOss(file);
        }
        return uploadToLocal(file);
    }

    private FileUploadResponseDTO uploadToOss(MultipartFile file) {
        OSS ossClient = ossClientProvider.getIfAvailable();
        if (ossClient == null || bucketName.isEmpty() || ossUrlPrefix.isEmpty()) {
            throw new BusinessException(500, "OSS 服务未配置或当前不可用");
        }

        log.info("开始上传文件到阿里云 OSS，文件名：{}", file.getOriginalFilename());
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String newFilename = UUID.randomUUID().toString().replace("-", "") + "." + extension;
            String ossKey = datePath + "/" + newFilename;

            ossClient.putObject(bucketName, ossKey, file.getInputStream());

            String finalUrl = ossUrlPrefix.endsWith("/") ? ossUrlPrefix : ossUrlPrefix + "/";
            finalUrl += ossKey;

            log.info("文件上传 OSS 成功，URL：{}", finalUrl);
            return FileUploadResponseDTO.builder().url(finalUrl).build();
        } catch (IOException e) {
            log.error("文件上传 OSS 失败", e);
            throw new BusinessException(500, "文件上传失败 (OSS): " + e.getMessage());
        }
    }

    private FileUploadResponseDTO uploadToLocal(MultipartFile file) {
        log.info("开始上传文件到本地磁盘，文件名：{}", file.getOriginalFilename());

        String originalFilename = file.getOriginalFilename();
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String extension = getFileExtension(originalFilename);
        String newFilename = UUID.randomUUID().toString().replace("-", "") + "." + extension;

        File baseDir = new File(uploadPath).getAbsoluteFile();
        File targetDir = new File(baseDir, datePath);
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            throw new BusinessException(500, "创建上传目录失败");
        }

        File destFile = new File(targetDir, newFilename);
        try {
            file.transferTo(destFile.getAbsoluteFile());
            log.info("文件上传本地成功，绝对路径：{}", destFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("文件上传本地失败", e);
            throw new BusinessException(500, "文件上传失败 (LOCAL): " + e.getMessage());
        }

        String finalUrl = fileUrlPrefix.endsWith("/") ? fileUrlPrefix : fileUrlPrefix + "/";
        finalUrl += datePath + "/" + newFilename;
        return FileUploadResponseDTO.builder().url(finalUrl).build();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请选择要上传的文件");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(400, "文件大小不能超过 5MB");
        }

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

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
