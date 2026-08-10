package com.example.food.delivery.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @NotBlank
    private String cuisine; // e.g. "Italian, Pizza, Fast Food"

    private String phone;

    private String email;

    @NotBlank
    private String address;

    @NotBlank
    private String city;

    private String state;

    private String pincode;

    private String landmark;

    private Double rating; // e.g. 4.5

    private Integer deliveryTimeMinutes; // e.g. 30

    private String priceRange; // e.g. "₹200 for two" or "$$"

    private String imageUrl;

    private LocalTime openingTime;

    private LocalTime closingTime;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OperationalStatus operationalStatus = OperationalStatus.OPEN;

    @Builder.Default
    private Boolean manualClosed = false;

    @Builder.Default
    private boolean active = true;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.rating == null) this.rating = 4.5;
        if (this.deliveryTimeMinutes == null) this.deliveryTimeMinutes = 30;
        if (this.operationalStatus == null) this.operationalStatus = OperationalStatus.OPEN;
        if (this.manualClosed == null) this.manualClosed = false;
    }

    public enum OperationalStatus {
        OPEN, CLOSED
    }

    public boolean isCurrentlyOpen() {
        if (!active) return false;
        if (Boolean.TRUE.equals(manualClosed) || operationalStatus == OperationalStatus.CLOSED) {
            return false;
        }
        if (openingTime != null && closingTime != null) {
            LocalTime now = LocalTime.now();
            if (openingTime.isBefore(closingTime)) {
                return !now.isBefore(openingTime) && !now.isAfter(closingTime);
            } else {
                // Overnight hours (e.g. 8 PM to 2 AM)
                return !now.isBefore(openingTime) || !now.isAfter(closingTime);
            }
        }
        return true;
    }
}

