package com.example.food.delivery.controller;

import com.example.food.delivery.dto.RestaurantFilterDto;
import com.example.food.delivery.entity.FoodItem;
import com.example.food.delivery.entity.Restaurant;
import com.example.food.delivery.entity.User;
import com.example.food.delivery.service.CartService;
import com.example.food.delivery.service.FoodService;
import com.example.food.delivery.service.RestaurantService;
import com.example.food.delivery.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final FoodService foodService;
    private final UserService userService;
    private final CartService cartService;

    @GetMapping
    public String listRestaurants(@ModelAttribute RestaurantFilterDto filter,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  Model model) {
        addUserAndCartToModel(userDetails, model);
        model.addAttribute("restaurants", restaurantService.filterRestaurants(filter));
        model.addAttribute("categories", foodService.getAllCategories());
        model.addAttribute("filter", filter);
        return "restaurants";
    }

    @GetMapping("/{id}")
    public String restaurantDetails(@PathVariable Long id,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    Model model) {
        addUserAndCartToModel(userDetails, model);
        Restaurant restaurant = restaurantService.getRestaurantById(id);
        List<FoodItem> foodItems = foodService.getFoodByRestaurant(restaurant);

        Map<String, List<FoodItem>> itemsByCategory = foodItems.stream()
                .collect(Collectors.groupingBy(item -> item.getCategory() != null ? item.getCategory().getName() : "General"));

        model.addAttribute("restaurant", restaurant);
        model.addAttribute("itemsByCategory", itemsByCategory);
        return "restaurant-details";
    }

    private void addUserAndCartToModel(UserDetails userDetails, Model model) {
        if (userDetails != null) {
            Optional<User> userOpt = userService.findByEmail(userDetails.getUsername());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                model.addAttribute("currentUser", user);
                model.addAttribute("cartItemCount", cartService.getCartSummary(user, null).getTotalItemCount());
                return;
            }
        }
        model.addAttribute("cartItemCount", 0);
    }
}
