package com.example.aiend.vo.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerRankingVO {
    private Integer rank;
    private Long userId;
    private String userName;
    private String avatar;
    private Integer completedOrders;
}
