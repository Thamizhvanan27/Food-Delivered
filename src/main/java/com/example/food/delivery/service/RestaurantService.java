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
}
