package com.example.aiend.controller.client;

import com.example.aiend.common.Result;
import com.example.aiend.dto.request.client.OcrRequestDTO;
import com.example.aiend.dto.response.client.IdCardOcrResponseDTO;
import com.example.aiend.service.client.OcrService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * OCR 识别控制器
 * 提供身份证自动识别接口供前端填表使用
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@Validated
@RequestMapping("/sys")
public class OcrController {

    private final OcrService ocrService;

    /**
     * 身份证 OCR 识别
     *
     * @param requestDTO 包含图片URL和正反面标识
     * @return 识别提取的结构化信息
     */
    @PostMapping("/ocr/idcard")
    public Result<IdCardOcrResponseDTO> recognizeIdCard(@Valid @RequestBody OcrRequestDTO requestDTO) {
        log.info("接收到身份证 OCR 识别请求，图片URL: {}, 识别面: {}", requestDTO.getImageUrl(), requestDTO.getSide());
        IdCardOcrResponseDTO response = ocrService.recognizeIdCard(requestDTO);
        return Result.success(response, "身份证识别成功");
    }
}
