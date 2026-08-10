package com.example.food.delivery.controller;

import com.example.food.delivery.entity.FoodCategory;
import com.example.food.delivery.entity.FoodItem;
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

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminFoodController {

    private final FoodService foodService;
    private final RestaurantService restaurantService;
    private final UserService userService;

    @GetMapping("/food")
    public String listFoodItems(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        model.addAttribute("currentUser", user);
        model.addAttribute("foodItems", foodService.getAllFoodItemsAdmin());
        return "admin/food-items";
    }

    @GetMapping("/food/new")
    public String newFoodItemForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        model.addAttribute("currentUser", user);
        model.addAttribute("foodItem", new FoodItem());
        model.addAttribute("restaurants", restaurantService.getAllRestaurantsAdmin());
        model.addAttribute("categories", foodService.getAllCategories());
        return "admin/food-form";
    }

    @GetMapping("/food/edit/{id}")
    public String editFoodItemForm(@PathVariable Long id,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   Model model) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        model.addAttribute("currentUser", user);
        model.addAttribute("foodItem", foodService.getFoodItemById(id));
        model.addAttribute("restaurants", restaurantService.getAllRestaurantsAdmin());
        model.addAttribute("categories", foodService.getAllCategories());
        return "admin/food-form";
    }

    @PostMapping("/food/save")
    public String saveFoodItem(@Valid @ModelAttribute("foodItem") FoodItem foodItem,
                               BindingResult result,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
            model.addAttribute("currentUser", user);
            model.addAttribute("restaurants", restaurantService.getAllRestaurantsAdmin());
            model.addAttribute("categories", foodService.getAllCategories());
            return "admin/food-form";
        }

        foodService.saveFoodItem(foodItem);
        redirectAttributes.addFlashAttribute("successMessage", "Food item saved successfully!");
        return "redirect:/admin/food";
    }

    @PostMapping("/food/toggle/{id}")
    public String toggleAvailability(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        foodService.toggleAvailability(id);
        redirectAttributes.addFlashAttribute("successMessage", "Food availability toggled!");
        return "redirect:/admin/food";
    }

    @PostMapping("/food/delete/{id}")
    public String deleteFoodItem(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            foodService.deleteFoodItem(id);
            redirectAttributes.addFlashAttribute("successMessage", "Food item deleted!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete food item referenced in orders.");
        }
        return "redirect:/admin/food";
    }

    @GetMapping("/categories")
    public String listCategories(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        model.addAttribute("currentUser", user);
        model.addAttribute("categories", foodService.getAllCategories());
        model.addAttribute("newCategory", new FoodCategory());
        return "admin/categories";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@Valid @ModelAttribute("newCategory") FoodCategory category,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please enter a valid category name.");
            return "redirect:/admin/categories";
        }

        foodService.saveCategory(category);
        redirectAttributes.addFlashAttribute("successMessage", "Category saved!");
        return "redirect:/admin/categories";
    }
}
