package com.example.food.delivery.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OwnerFoodItemDto {

    private Long id;

    @NotNull(message = "Restaurant selection is required")
    private Long restaurantId;

    @NotBlank(message = "Food name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    private BigDecimal price;

    private Long categoryId;

    private String imageUrl;

    private boolean isVegetarian = true;

    private boolean available = true;

    private Integer prepTimeMinutes = 20;

    private String specialInstructions;
}
