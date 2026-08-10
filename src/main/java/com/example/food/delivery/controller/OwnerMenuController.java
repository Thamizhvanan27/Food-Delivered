package com.example.food.delivery.controller;

import com.example.food.delivery.dto.OwnerFoodItemDto;
import com.example.food.delivery.entity.FoodCategory;
import com.example.food.delivery.entity.FoodItem;
import com.example.food.delivery.entity.Restaurant;
import com.example.food.delivery.entity.User;
import com.example.food.delivery.service.FoodService;
import com.example.food.delivery.service.RestaurantService;
import com.example.food.delivery.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/owner/menu")
@RequiredArgsConstructor
public class OwnerMenuController {

    private final UserService userService;
    private final RestaurantService restaurantService;
    private final FoodService foodService;

    private User getLoggedOwner(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @GetMapping
    public String listMenuItems(@RequestParam(required = false) Long restaurantId,
                                @AuthenticationPrincipal UserDetails userDetails,
                                Model model) {
        User owner = getLoggedOwner(userDetails);
        List<Restaurant> ownerRestaurants = restaurantService.getRestaurantsByOwner(owner);

        if (ownerRestaurants.isEmpty()) {
            model.addAttribute("noRestaurant", true);
            return "owner/menu";
        }

        Restaurant selectedRestaurant;
        if (restaurantId != null) {
            selectedRestaurant = restaurantService.getRestaurantByIdAndOwner(restaurantId, owner);
        } else {
            selectedRestaurant = ownerRestaurants.get(0);
        }

        List<FoodItem> foodItems = foodService.getAllFoodByRestaurant(selectedRestaurant);

        model.addAttribute("ownerRestaurants", ownerRestaurants);
        model.addAttribute("selectedRestaurant", selectedRestaurant);
        model.addAttribute("foodItems", foodItems);
        return "owner/menu";
    }

    @GetMapping("/new")
    public String showCreateForm(@RequestParam(required = false) Long restaurantId,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model) {
        User owner = getLoggedOwner(userDetails);
        List<Restaurant> restaurants = restaurantService.getRestaurantsByOwner(owner);

        if (restaurants.isEmpty()) {
            return "redirect:/owner/restaurants/new";
        }

        OwnerFoodItemDto dto = new OwnerFoodItemDto();
        if (restaurantId != null) {
            dto.setRestaurantId(restaurantId);
        } else if (!restaurants.isEmpty()) {
            dto.setRestaurantId(restaurants.get(0).getId());
        }

        List<FoodCategory> categories = foodService.getAllCategories();

        model.addAttribute("foodDto", dto);
        model.addAttribute("restaurants", restaurants);
        model.addAttribute("categories", categories);
        model.addAttribute("isEdit", false);
        return "owner/food-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        User owner = getLoggedOwner(userDetails);
        FoodItem item = foodService.getFoodItemByIdAndOwner(id, owner);
        List<Restaurant> restaurants = restaurantService.getRestaurantsByOwner(owner);
        List<FoodCategory> categories = foodService.getAllCategories();

        OwnerFoodItemDto dto = new OwnerFoodItemDto();
        dto.setId(item.getId());
        dto.setRestaurantId(item.getRestaurant().getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setPrice(item.getPrice());
        dto.setImageUrl(item.getImageUrl());
        dto.setVegetarian(item.isVegetarian());
        dto.setAvailable(item.isAvailable());
        dto.setPrepTimeMinutes(item.getPrepTimeMinutes());
        dto.setSpecialInstructions(item.getSpecialInstructions());
        if (item.getCategory() != null) {
            dto.setCategoryId(item.getCategory().getId());
        }

        model.addAttribute("foodDto", dto);
        model.addAttribute("restaurants", restaurants);
        model.addAttribute("categories", categories);
        model.addAttribute("isEdit", true);
        return "owner/food-form";
    }

    @PostMapping("/save")
    public String saveFoodItem(@Valid @ModelAttribute("foodDto") OwnerFoodItemDto dto,
                               BindingResult bindingResult,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        User owner = getLoggedOwner(userDetails);
        List<Restaurant> restaurants = restaurantService.getRestaurantsByOwner(owner);

        if (bindingResult.hasErrors()) {
            model.addAttribute("restaurants", restaurants);
            model.addAttribute("categories", foodService.getAllCategories());
            model.addAttribute("isEdit", dto.getId() != null);
            return "owner/food-form";
        }

        try {
            Restaurant restaurant = restaurantService.getRestaurantByIdAndOwner(dto.getRestaurantId(), owner);
            foodService.saveOwnerFoodItem(dto, owner, restaurant);
            redirectAttributes.addFlashAttribute("success", "Menu item saved successfully!");
            return "redirect:/owner/menu?restaurantId=" + restaurant.getId();
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("restaurants", restaurants);
            model.addAttribute("categories", foodService.getAllCategories());
            model.addAttribute("isEdit", dto.getId() != null);
            return "owner/food-form";
        }
    }

    @PostMapping("/toggle-availability/{id}")
    public String toggleAvailability(@PathVariable Long id,
                                     @AuthenticationPrincipal UserDetails userDetails,
                                     RedirectAttributes redirectAttributes) {
        User owner = getLoggedOwner(userDetails);
        try {
            FoodItem item = foodService.getFoodItemByIdAndOwner(id, owner);
            foodService.toggleFoodAvailabilityByOwner(id, owner);
            redirectAttributes.addFlashAttribute("success", "Menu item availability updated!");
            return "redirect:/owner/menu?restaurantId=" + item.getRestaurant().getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/owner/menu";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteFoodItem(@PathVariable Long id,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        User owner = getLoggedOwner(userDetails);
        try {
            FoodItem item = foodService.getFoodItemByIdAndOwner(id, owner);
            Long restId = item.getRestaurant().getId();
            foodService.deleteOwnerFoodItem(id, owner);
            redirectAttributes.addFlashAttribute("success", "Menu item deleted successfully!");
            return "redirect:/owner/menu?restaurantId=" + restId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/owner/menu";
        }
    }
}
