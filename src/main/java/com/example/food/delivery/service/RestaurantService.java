package com.example.food.delivery.service;

import com.example.food.delivery.dto.RestaurantFilterDto;
import com.example.food.delivery.entity.Restaurant;
import com.example.food.delivery.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public List<Restaurant> getActiveRestaurants() {
        return restaurantRepository.findByActiveTrue();
    }

    public List<Restaurant> getAllRestaurantsAdmin() {
        return restaurantRepository.findAll();
    }

    public Restaurant getRestaurantById(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found with id: " + id));
    }

    public List<Restaurant> filterRestaurants(RestaurantFilterDto filter) {
        List<Restaurant> list = restaurantRepository.findByActiveTrue();

        if (filter.getQuery() != null && !filter.getQuery().trim().isEmpty()) {
            String q = filter.getQuery().toLowerCase().trim();
            list = list.stream()
                    .filter(r -> r.getName().toLowerCase().contains(q) ||
                            r.getCuisine().toLowerCase().contains(q) ||
                            r.getCity().toLowerCase().contains(q))
                    .collect(Collectors.toList());
        }

        if (filter.getCuisine() != null && !filter.getCuisine().trim().isEmpty() && !filter.getCuisine().equalsIgnoreCase("ALL")) {
            String c = filter.getCuisine().toLowerCase().trim();
            list = list.stream()
                    .filter(r -> r.getCuisine().toLowerCase().contains(c))
                    .collect(Collectors.toList());
        }

        if (filter.getMinRating() != null) {
            list = list.stream()
                    .filter(r -> r.getRating() != null && r.getRating() >= filter.getMinRating())
                    .collect(Collectors.toList());
        }

        if (filter.getSortBy() != null) {
            switch (filter.getSortBy().toLowerCase()) {
                case "rating":
                    list.sort(Comparator.comparing(Restaurant::getRating, Comparator.nullsLast(Comparator.reverseOrder())));
                    break;
                case "deliverytime":
                    list.sort(Comparator.comparing(Restaurant::getDeliveryTimeMinutes, Comparator.nullsLast(Comparator.naturalOrder())));
                    break;
                case "name":
                    list.sort(Comparator.comparing(Restaurant::getName, String.CASE_INSENSITIVE_ORDER));
                    break;
            }
        }

        return list;
    }

    @Transactional
    public Restaurant saveRestaurant(Restaurant restaurant) {
        return restaurantRepository.save(restaurant);
    }

    @Transactional
    public void toggleActiveStatus(Long id) {
        Restaurant restaurant = getRestaurantById(id);
        restaurant.setActive(!restaurant.isActive());
        restaurantRepository.save(restaurant);
    }

    @Transactional
    public void deleteRestaurant(Long id) {
        restaurantRepository.deleteById(id);
    }

    public List<Restaurant> getRestaurantsByOwner(com.example.food.delivery.entity.User owner) {
        return restaurantRepository.findByOwner(owner);
    }

    public Restaurant getRestaurantByIdAndOwner(Long id, com.example.food.delivery.entity.User owner) {
        return restaurantRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Access denied or restaurant not found for this owner."));
    }

    @Transactional
    public Restaurant saveOwnerRestaurant(com.example.food.delivery.dto.OwnerRestaurantDto dto, com.example.food.delivery.entity.User owner) {
        Restaurant restaurant;
        if (dto.getId() != null) {
            restaurant = getRestaurantByIdAndOwner(dto.getId(), owner);
        } else {
            restaurant = new Restaurant();
            restaurant.setOwner(owner);
        }

        restaurant.setName(dto.getName());
        restaurant.setDescription(dto.getDescription());
        restaurant.setCuisine(dto.getCuisine());
        restaurant.setPhone(dto.getPhone());
        restaurant.setEmail(dto.getEmail());
        restaurant.setAddress(dto.getAddress());
        restaurant.setCity(dto.getCity());
        restaurant.setState(dto.getState());
        restaurant.setPincode(dto.getPincode());
        restaurant.setLandmark(dto.getLandmark());
        restaurant.setPriceRange(dto.getPriceRange());
        if (dto.getDeliveryTimeMinutes() != null) restaurant.setDeliveryTimeMinutes(dto.getDeliveryTimeMinutes());
        if (dto.getImageUrl() != null && !dto.getImageUrl().trim().isEmpty()) restaurant.setImageUrl(dto.getImageUrl());
        restaurant.setOpeningTime(dto.getOpeningTime());
        restaurant.setClosingTime(dto.getClosingTime());
        if (dto.getOperationalStatus() != null) restaurant.setOperationalStatus(dto.getOperationalStatus());
        if (dto.getManualClosed() != null) restaurant.setManualClosed(dto.getManualClosed());

        return restaurantRepository.save(restaurant);
    }

    @Transactional
    public void toggleOperationalStatus(Long id, com.example.food.delivery.entity.User owner) {
        Restaurant restaurant = getRestaurantByIdAndOwner(id, owner);
        if (restaurant.getOperationalStatus() == Restaurant.OperationalStatus.OPEN) {
            restaurant.setOperationalStatus(Restaurant.OperationalStatus.CLOSED);
            restaurant.setManualClosed(true);
        } else {
            restaurant.setOperationalStatus(Restaurant.OperationalStatus.OPEN);
            restaurant.setManualClosed(false);
        }
        restaurantRepository.save(restaurant);
    }
}
