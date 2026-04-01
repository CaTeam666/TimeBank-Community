package com.example.aiend.config;

import com.aliyun.ocr_api20210707.Client;
import com.aliyun.teaopenapi.models.Config;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "aliyun")
public class AliyunOcrConfig {
    
    private OcrConfig ocr;
    private OssConfig oss;

    @Data
    public static class OcrConfig {
        private String accessKeyId;
        private String accessKeySecret;
        private String endpoint;
    }

    @Data
    public static class OssConfig {
        private String accessKeyId;
        private String accessKeySecret;
        private String endpoint;
        private String bucketName;
        private String urlPrefix;
    }

    @Bean
    public com.aliyun.ocr_api20210707.Client ocrClient() throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                .setAccessKeyId(ocr.getAccessKeyId())
                .setAccessKeySecret(ocr.getAccessKeySecret());
        config.endpoint = ocr.getEndpoint();
        return new com.aliyun.ocr_api20210707.Client(config);
    }

    @Bean
    public com.aliyun.oss.OSS ossClient() {
        // 使用 OCR 的密钥，如果 OSS 配置中没配的话
        String ak = oss.getAccessKeyId() != null ? oss.getAccessKeyId() : ocr.getAccessKeyId();
        String sk = oss.getAccessKeySecret() != null ? oss.getAccessKeySecret() : ocr.getAccessKeySecret();
        return new com.aliyun.oss.OSSClientBuilder().build(oss.getEndpoint(), ak, sk);
    }
}
