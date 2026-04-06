package com.example.aiend.service.client.impl;

import com.aliyun.ocr_api20210707.Client;
import com.aliyun.ocr_api20210707.models.RecognizeIdcardRequest;
import com.aliyun.ocr_api20210707.models.RecognizeIdcardResponse;
import com.aliyun.oss.OSS;
import com.example.aiend.common.exception.BusinessException;
import com.example.aiend.dto.request.client.OcrRequestDTO;
import com.example.aiend.dto.response.client.IdCardOcrResponseDTO;
import com.example.aiend.service.client.OcrService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OcrServiceImpl implements OcrService {

    private final ObjectProvider<Client> ocrClientProvider;
    private final ObjectProvider<OSS> ossClientProvider;
    private final ObjectMapper objectMapper;

    @Value("${aliyun.oss.bucket-name:}")
    private String bucketName;

    @Value("${aliyun.oss.url-prefix:}")
    private String urlPrefix;

    @Override
    public IdCardOcrResponseDTO recognizeIdCard(OcrRequestDTO requestDTO) {
        Client ocrClient = ocrClientProvider.getIfAvailable();
        if (ocrClient == null) {
            throw new BusinessException(500, "OCR 服务未配置，请先设置阿里云 OCR 密钥");
        }

        OSS ossClient = ossClientProvider.getIfAvailable();
        String originalUrl = requestDTO.getImageUrl();
        String finalUrl = originalUrl;
        log.info("开始调用阿里云 OCR 识别身份证，原始图片：{}，面：{}", originalUrl, requestDTO.getSide());

        if (ossClient != null && originalUrl != null && originalUrl.startsWith(urlPrefix)) {
            try {
                String ossKey = originalUrl.substring(urlPrefix.length());
                if (ossKey.startsWith("/")) {
                    ossKey = ossKey.substring(1);
                }

                java.util.Date expiration = new java.util.Date(System.currentTimeMillis() + 60 * 1000);
                java.net.URL signedUrl = ossClient.generatePresignedUrl(bucketName, ossKey, expiration);
                finalUrl = signedUrl.toString();
                log.info("已生成用于 OCR 的临时签名 URL");
            } catch (Exception e) {
                log.warn("生成 OSS 签名 URL 失败，回退到原始 URL", e);
            }
        }

        try {
            RecognizeIdcardRequest request = new RecognizeIdcardRequest();
            request.setUrl(finalUrl);

            RecognizeIdcardResponse response = ocrClient.recognizeIdcard(request);
            if (response.getBody() == null || response.getBody().getData() == null) {
                throw new BusinessException(500, "OCR 识别失败，返回数据为空");
            }

            Object dataObj = response.getBody().getData();
            String dataStr = dataObj instanceof String ? (String) dataObj : objectMapper.writeValueAsString(dataObj);
            JsonNode rootNode = objectMapper.readTree(dataStr);
            JsonNode dataNode = rootNode.has("data") && !rootNode.get("data").isValueNode()
                    ? rootNode.get("data")
                    : rootNode;

            IdCardOcrResponseDTO.IdCardOcrResponseDTOBuilder builder = IdCardOcrResponseDTO.builder();
            if ("face".equalsIgnoreCase(requestDTO.getSide())) {
                JsonNode faceData = dataNode.path("face").path("data");
                if (faceData.isMissingNode() || faceData.isNull()) {
                    throw new BusinessException(400, "无法从 OCR 响应中解析身份证正面信息");
                }
                if (faceData.has("name")) {
                    builder.name(faceData.get("name").asText());
                }
                if (faceData.has("idNumber")) {
                    builder.idNum(faceData.get("idNumber").asText());
                }
                if (faceData.has("sex")) {
                    builder.sex(faceData.get("sex").asText());
                }
                if (faceData.has("birthDate")) {
                    builder.birth(faceData.get("birthDate").asText());
                }
                if (faceData.has("address")) {
                    builder.address(faceData.get("address").asText());
                }
                if (faceData.has("ethnicity")) {
                    builder.nationality(faceData.get("ethnicity").asText());
                } else if (faceData.has("nationality")) {
                    builder.nationality(faceData.get("nationality").asText());
                }
            } else if ("back".equalsIgnoreCase(requestDTO.getSide())) {
                JsonNode backData = dataNode.path("back").path("data");
                if (backData.isMissingNode() || backData.isNull()) {
                    throw new BusinessException(400, "无法从 OCR 响应中解析身份证反面信息");
                }
                if (backData.has("issue")) {
                    builder.issue(backData.get("issue").asText());
                }
                if (backData.has("startDate")) {
                    builder.startDate(backData.get("startDate").asText());
                }
                if (backData.has("endDate")) {
                    builder.endDate(backData.get("endDate").asText());
                }
            }

            builder.side(requestDTO.getSide());
            return builder.build();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用阿里云 OCR 发生异常", e);
            throw new BusinessException(500, "身份证识别失败: " + e.getMessage());
        }
    }
}
