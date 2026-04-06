package com.example.aiend.config;

import com.aliyun.ocr_api20210707.Client;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
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
    @ConditionalOnExpression(
            "'${aliyun.ocr.access-key-id:}' != '' and '${aliyun.ocr.access-key-secret:}' != '' and '${aliyun.ocr.endpoint:}' != ''"
    )
    public Client ocrClient() throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                .setAccessKeyId(ocr.getAccessKeyId())
                .setAccessKeySecret(ocr.getAccessKeySecret());
        config.endpoint = ocr.getEndpoint();
        return new Client(config);
    }

    @Bean
    @ConditionalOnExpression(
            "'${aliyun.oss.endpoint:}' != '' and (('${aliyun.oss.access-key-id:}' != '' and '${aliyun.oss.access-key-secret:}' != '') or ('${aliyun.ocr.access-key-id:}' != '' and '${aliyun.ocr.access-key-secret:}' != ''))"
    )
    public com.aliyun.oss.OSS ossClient() {
        String ak = hasText(oss.getAccessKeyId()) ? oss.getAccessKeyId() : ocr.getAccessKeyId();
        String sk = hasText(oss.getAccessKeySecret()) ? oss.getAccessKeySecret() : ocr.getAccessKeySecret();
        return new com.aliyun.oss.OSSClientBuilder().build(oss.getEndpoint(), ak, sk);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
