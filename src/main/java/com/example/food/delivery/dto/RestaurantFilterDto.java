package com.example.food.delivery.dto;

import lombok.Data;

@Data
public class RestaurantFilterDto {
    private String query;
    private String cuisine;
    private Double minRating;
    private String priceRange;
    private Boolean isVegOnly;
    private String sortBy; // rating, deliveryTime, name
}
