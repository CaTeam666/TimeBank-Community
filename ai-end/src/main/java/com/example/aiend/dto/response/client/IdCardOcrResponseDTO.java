package com.example.aiend.dto.response.client;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IdCardOcrResponseDTO {
    private String name;
    private String idNum;
    private String sex;
    private String birth;
    private String address;
    private String nationality;
    private String issue;
    private String startDate;
    private String endDate;
    private String side;
}
