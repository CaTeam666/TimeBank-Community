package com.example.aiend.service.client;

import com.example.aiend.dto.request.client.OcrRequestDTO;
import com.example.aiend.dto.response.client.IdCardOcrResponseDTO;

public interface OcrService {
    IdCardOcrResponseDTO recognizeIdCard(OcrRequestDTO requestDTO);
}
