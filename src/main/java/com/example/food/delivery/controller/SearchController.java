package com.example.food.delivery.controller;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final RestaurantService restaurantService;
    private final FoodService foodService;
    private final UserService userService;
    private final CartService cartService;

    @GetMapping("/search")
    public String search(@RequestParam(value = "q", required = false, defaultValue = "") String query,
                         @AuthenticationPrincipal UserDetails userDetails,
                         Model model) {
        addUserAndCartToModel(userDetails, model);

        String trimmedQuery = query.trim();
        List<Restaurant> matchingRestaurants = restaurantService.filterRestaurants(
                new com.example.food.delivery.dto.RestaurantFilterDto() {{ setQuery(trimmedQuery); }}
        );
        List<FoodItem> matchingFood = foodService.searchFoodItems(trimmedQuery);

        model.addAttribute("query", trimmedQuery);
        model.addAttribute("matchingRestaurants", matchingRestaurants);
        model.addAttribute("matchingFood", matchingFood);
        return "search-results";
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
