package com.example.food.delivery.dto;

import com.example.food.delivery.entity.CartItem;
import com.example.food.delivery.entity.Restaurant;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CartSummaryDto {
    private List<CartItem> items;
    private Restaurant restaurant;
    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal tax;
    private BigDecimal discountAmount;
    private String couponCode;
    private BigDecimal grandTotal;
    private int totalItemCount;
}
