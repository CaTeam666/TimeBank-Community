package com.example.aiend.service.client.impl;

import com.aliyun.ocr_api20210707.Client;
import com.aliyun.ocr_api20210707.models.RecognizeIdcardRequest;
import com.aliyun.ocr_api20210707.models.RecognizeIdcardResponse;
import com.example.aiend.common.exception.BusinessException;
import com.example.aiend.dto.request.client.OcrRequestDTO;
import com.example.aiend.dto.response.client.IdCardOcrResponseDTO;
import com.example.aiend.service.client.OcrService;
import com.aliyun.oss.OSS;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OcrServiceImpl implements OcrService {

    private final Client ocrClient;
    private final OSS ossClient;
    private final ObjectMapper objectMapper;

    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;

    @Value("${aliyun.oss.url-prefix}")
    private String urlPrefix;

    @Override
    public IdCardOcrResponseDTO recognizeIdCard(OcrRequestDTO requestDTO) {
        String originalUrl = requestDTO.getImageUrl();
        log.info("开始调用阿里OCR识别身份证，原始图片：{}，面：{}", originalUrl, requestDTO.getSide());
        
        String finalUrl = originalUrl;
        
        // 如果图片是存储在我们的 OSS 中，生成一个有效期为 1 分钟的带签名 URL，提高安全性
        if (originalUrl != null && originalUrl.startsWith(urlPrefix)) {
            try {
                // 提取 OSS 中的文件路径 (Key)
                String ossKey = originalUrl.substring(urlPrefix.length());
                if (ossKey.startsWith("/")) {
                    ossKey = ossKey.substring(1);
                }
                
                // 设置过期时间为 1 分钟后
                java.util.Date expiration = new java.util.Date(System.currentTimeMillis() + 60 * 1000);
                
                // 生成签名 URL
                java.net.URL signedUrl = ossClient.generatePresignedUrl(bucketName, ossKey, expiration);
                finalUrl = signedUrl.toString();
                
                log.info("已生成带签名的临时 URL (1分钟有效)，用于 OCR 识别");
                log.debug("签名 URL: {}", finalUrl);
            } catch (Exception e) {
                log.warn("生成 OSS 签名 URL 失败，降级使用原始 URL", e);
            }
        }

        try {
            RecognizeIdcardRequest request = new RecognizeIdcardRequest();
            request.setUrl(finalUrl);
            
            RecognizeIdcardResponse response = ocrClient.recognizeIdcard(request);
            if (response.getBody() == null || response.getBody().getData() == null) {
                throw new BusinessException(500, "OCR识别失败，返回数据为空");
            }
            
            Object dataObj = response.getBody().getData();
            String dataStr = dataObj instanceof String ? (String) dataObj : objectMapper.writeValueAsString(dataObj);
            log.info("Aliyun OCR 原始响应数据: {}", dataStr);
            JsonNode rootNode = objectMapper.readTree(dataStr);
            // 阿里接口返回的数据有时嵌套在 "data" 节点下
            JsonNode dataNode = rootNode.has("data") && !rootNode.get("data").isValueNode() ? rootNode.get("data") : rootNode;
            
            IdCardOcrResponseDTO.IdCardOcrResponseDTOBuilder builder = IdCardOcrResponseDTO.builder();
            
            if ("face".equalsIgnoreCase(requestDTO.getSide())) {
                if (dataNode.has("face") && !dataNode.get("face").isNull()) {
                    JsonNode faceNode = dataNode.get("face");
                    if (faceNode.has("data") && !faceNode.get("data").isNull()) {
                       JsonNode info = faceNode.get("data");
                       if(info.has("name")) builder.name(info.get("name").asText());
                       if(info.has("idNumber")) builder.idNum(info.get("idNumber").asText());
                       if(info.has("sex")) builder.sex(info.get("sex").asText());
                       if(info.has("birthDate")) builder.birth(info.get("birthDate").asText());
                       if(info.has("address")) builder.address(info.get("address").asText());
                       // 阿里 JSON 中字段名可能是 ethnicity
                       if(info.has("ethnicity")) builder.nationality(info.get("ethnicity").asText());
                       else if(info.has("nationality")) builder.nationality(info.get("nationality").asText());
                    }
                } else {
                    log.error("解析失败，dataNode 内容: {}", dataNode.toString());
                    throw new BusinessException(400, "无法从响应中解析出身份证正面信息");
                }
            } else if ("back".equalsIgnoreCase(requestDTO.getSide())) {
                if (dataNode.has("back") && !dataNode.get("back").isNull()) {
                    JsonNode backNode = dataNode.get("back");
                    if(backNode.has("data") && !backNode.get("data").isNull()){
                        JsonNode info = backNode.get("data");
                        if(info.has("issue")) builder.issue(info.get("issue").asText());
                        if(info.has("startDate")) builder.startDate(info.get("startDate").asText());
                        if(info.has("endDate")) builder.endDate(info.get("endDate").asText());
                    }
                } else {
                    log.error("解析失败，dataNode 内容: {}", dataNode.toString());
                    throw new BusinessException(400, "无法从响应中解析出身份证反面信息");
                }
            }
            
            builder.side(requestDTO.getSide());
            return builder.build();

        } catch (Exception e) {
            log.error("调用阿里OCR发生异常", e);
            throw new BusinessException(500, "身份证识别失败: " + e.getMessage());
        }
    }
}
