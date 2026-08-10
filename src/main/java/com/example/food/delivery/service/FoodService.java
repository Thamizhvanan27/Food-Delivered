package com.example.food.delivery.service;

import com.example.food.delivery.entity.FoodCategory;
import com.example.food.delivery.entity.FoodItem;
import com.example.food.delivery.entity.Restaurant;
import com.example.food.delivery.repository.FoodCategoryRepository;
import com.example.food.delivery.repository.FoodItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodService {

    private final FoodItemRepository foodItemRepository;
    private final FoodCategoryRepository foodCategoryRepository;

    public List<FoodItem> getFoodByRestaurant(Restaurant restaurant) {
        return foodItemRepository.findByRestaurantAndAvailableTrue(restaurant);
    }

    public List<FoodCategory> getAllCategories() {
        return foodCategoryRepository.findAll();
    }

    public FoodCategory getCategoryById(Long id) {
        return foodCategoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));
    }

    @Transactional
    public FoodCategory saveCategory(FoodCategory category) {
        return foodCategoryRepository.save(category);
    }

    public FoodItem getFoodItemById(Long id) {
        return foodItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Food item not found with id: " + id));
    }

    public List<FoodItem> searchFoodItems(String query) {
        return foodItemRepository.searchFoodItems(query);
    }

    public List<FoodItem> getTrendingFoodItems() {
        return foodItemRepository.findTop10ByAvailableTrueOrderByIdDesc();
    }

    public List<FoodItem> getAllFoodItemsAdmin() {
        return foodItemRepository.findAll();
    }

    @Transactional
    public FoodItem saveFoodItem(FoodItem foodItem) {
        return foodItemRepository.save(foodItem);
    }

    @Transactional
    public void toggleAvailability(Long id) {
        FoodItem foodItem = getFoodItemById(id);
        foodItem.setAvailable(!foodItem.isAvailable());
        foodItemRepository.save(foodItem);
    }

    @Transactional
    public void deleteFoodItem(Long id) {
        foodItemRepository.deleteById(id);
    }
}
