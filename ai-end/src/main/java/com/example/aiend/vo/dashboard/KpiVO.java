package com.example.aiend.vo.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiVO {
    private Long totalPopulation;
    private Integer todayExchangeCount;
    private Integer todayNewOrders;
    private Integer todayPointsCirculation;
}
