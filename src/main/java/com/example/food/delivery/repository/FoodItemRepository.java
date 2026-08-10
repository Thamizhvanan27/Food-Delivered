package com.example.food.delivery.repository;

import com.example.food.delivery.entity.FoodCategory;
import com.example.food.delivery.entity.FoodItem;
import com.example.food.delivery.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {
    List<FoodItem> findByRestaurantAndAvailableTrue(Restaurant restaurant);
    List<FoodItem> findByCategoryAndAvailableTrue(FoodCategory category);
    
    @Query("SELECT f FROM FoodItem f WHERE f.available = true AND " +
           "(LOWER(f.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(f.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<FoodItem> searchFoodItems(@Param("query") String query);

    List<FoodItem> findTop10ByAvailableTrueOrderByIdDesc();

    List<FoodItem> findByRestaurant(Restaurant restaurant);

    List<FoodItem> findByRestaurantOwner(com.example.food.delivery.entity.User owner);

    List<FoodItem> findByRestaurantIdAndRestaurantOwner(Long restaurantId, com.example.food.delivery.entity.User owner);

    java.util.Optional<FoodItem> findByIdAndRestaurantOwner(Long id, com.example.food.delivery.entity.User owner);

    long countByRestaurantOwner(com.example.food.delivery.entity.User owner);
}
