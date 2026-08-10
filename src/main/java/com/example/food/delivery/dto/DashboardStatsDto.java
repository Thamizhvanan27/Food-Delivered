package com.example.food.delivery.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DashboardStatsDto {
    private long totalUsers;
    private long totalRestaurants;
    private long totalFoodItems;
    private long totalOrders;
    private long todayOrders;
    private BigDecimal todayRevenue;
    private BigDecimal totalRevenue;
    private long pendingOrders;
    private long deliveredOrders;
    private long cancelledOrders;
}
