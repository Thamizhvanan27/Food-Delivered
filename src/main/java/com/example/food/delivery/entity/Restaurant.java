package com.example.food.delivery.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "restaurants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @NotBlank
    private String cuisine; // e.g. "Italian, Pizza, Fast Food"

    @NotBlank
    private String address;

    @NotBlank
    private String city;

    private Double rating; // e.g. 4.5

    private Integer deliveryTimeMinutes; // e.g. 30

    private String priceRange; // e.g. "₹200 for two" or "$$"

    private String imageUrl;

    @Builder.Default
    private boolean active = true;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.rating == null) this.rating = 4.5;
        if (this.deliveryTimeMinutes == null) this.deliveryTimeMinutes = 30;
    }
}
