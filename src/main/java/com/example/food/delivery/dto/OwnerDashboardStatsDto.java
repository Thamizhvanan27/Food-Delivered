package com.example.food.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerDashboardStatsDto {
    private long totalRestaurants;
    private long totalMenuItems;
    private long todaysOrders;
    private long pendingOrders;
    private long completedOrders;
    private BigDecimal todaysRevenue;
}
