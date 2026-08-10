package com.example.food.delivery.repository;

import com.example.food.delivery.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findByActiveTrue();
    
    @Query("SELECT r FROM Restaurant r WHERE r.active = true AND " +
           "(LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(r.cuisine) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(r.city) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Restaurant> searchRestaurants(@Param("query") String query);

    List<Restaurant> findByActiveTrueOrderByRatingDesc();
}
