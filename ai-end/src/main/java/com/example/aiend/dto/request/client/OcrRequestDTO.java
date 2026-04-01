package com.example.aiend.dto.request.client;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OcrRequestDTO {
    @NotBlank(message = "图片URL不能为空")
    private String imageUrl;

    @NotBlank(message = "身份证正反面(face/back)不能为空")
    private String side;
}
