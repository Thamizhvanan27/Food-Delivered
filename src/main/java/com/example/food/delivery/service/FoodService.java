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

    public List<FoodItem> getAllFoodByRestaurant(Restaurant restaurant) {
        return foodItemRepository.findByRestaurant(restaurant);
    }

    public List<FoodItem> getFoodItemsByOwner(com.example.food.delivery.entity.User owner) {
        return foodItemRepository.findByRestaurantOwner(owner);
    }

    public FoodItem getFoodItemByIdAndOwner(Long id, com.example.food.delivery.entity.User owner) {
        return foodItemRepository.findByIdAndRestaurantOwner(id, owner)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Access denied or food item not found for this owner."));
    }

    @Transactional
    public FoodItem saveOwnerFoodItem(com.example.food.delivery.dto.OwnerFoodItemDto dto, com.example.food.delivery.entity.User owner, Restaurant restaurant) {
        FoodItem item;
        if (dto.getId() != null) {
            item = getFoodItemByIdAndOwner(dto.getId(), owner);
        } else {
            item = new FoodItem();
            item.setRestaurant(restaurant);
        }

        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setPrice(dto.getPrice());
        item.setImageUrl(dto.getImageUrl());
        item.setVegetarian(dto.isVegetarian());
        item.setAvailable(dto.isAvailable());
        item.setPrepTimeMinutes(dto.getPrepTimeMinutes());
        item.setSpecialInstructions(dto.getSpecialInstructions());

        if (dto.getCategoryId() != null) {
            item.setCategory(getCategoryById(dto.getCategoryId()));
        }

        return foodItemRepository.save(item);
    }

    @Transactional
    public void toggleFoodAvailabilityByOwner(Long id, com.example.food.delivery.entity.User owner) {
        FoodItem item = getFoodItemByIdAndOwner(id, owner);
        item.setAvailable(!item.isAvailable());
        foodItemRepository.save(item);
    }

    @Transactional
    public void deleteOwnerFoodItem(Long id, com.example.food.delivery.entity.User owner) {
        FoodItem item = getFoodItemByIdAndOwner(id, owner);
        foodItemRepository.delete(item);
    }
}
