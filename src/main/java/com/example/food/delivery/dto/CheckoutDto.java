package com.example.food.delivery.dto;

import com.example.food.delivery.entity.Order;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckoutDto {

    @NotNull(message = "Please select a delivery address")
    private Long addressId;

    private Order.PaymentMethod paymentMethod = Order.PaymentMethod.ONLINE;

    private String couponCode;
}
