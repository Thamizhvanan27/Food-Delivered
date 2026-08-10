package com.example.food.delivery.controller;

import com.example.food.delivery.entity.User;
import com.example.food.delivery.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final RestaurantService restaurantService;
    private final FoodService foodService;
    private final CouponService couponService;
    private final UserService userService;
    private final CartService cartService;

    @GetMapping({"/", "/home"})
    public String home(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        addUserAndCartToModel(userDetails, model);
        model.addAttribute("restaurants", restaurantService.getActiveRestaurants());
        model.addAttribute("categories", foodService.getAllCategories());
        model.addAttribute("trendingFoods", foodService.getTrendingFoodItems());
        model.addAttribute("coupons", couponService.getActiveCoupons());
        return "index";
    }

    @GetMapping("/offers")
    public String offers(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        addUserAndCartToModel(userDetails, model);
        model.addAttribute("coupons", couponService.getActiveCoupons());
        return "offers";
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
