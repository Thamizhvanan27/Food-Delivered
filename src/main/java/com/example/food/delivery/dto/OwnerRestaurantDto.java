package com.example.food.delivery.dto;

import com.example.food.delivery.entity.Restaurant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;

@Data
public class OwnerRestaurantDto {

    private Long id;

    @NotBlank(message = "Restaurant name is required")
    private String name;

    private String description;

    @NotBlank(message = "Cuisine type is required")
    private String cuisine;

    @NotBlank(message = "Phone number is required")
    private String phone;

    private String email;

    @NotBlank(message = "Street address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    private String state;

    private String pincode;

    private String landmark;

    private String priceRange;

    private Integer deliveryTimeMinutes;

    private String imageUrl;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime openingTime;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime closingTime;

    private Restaurant.OperationalStatus operationalStatus = Restaurant.OperationalStatus.OPEN;

    private Boolean manualClosed = false;
}
