package com.example.food.delivery.repository;

import com.example.food.delivery.entity.Cart;
import com.example.food.delivery.entity.CartItem;
import com.example.food.delivery.entity.FoodItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndFoodItem(Cart cart, FoodItem foodItem);
}
