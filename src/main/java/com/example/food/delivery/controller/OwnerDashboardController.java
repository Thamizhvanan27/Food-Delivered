package com.example.food.delivery.controller;

import com.example.food.delivery.dto.OwnerDashboardStatsDto;
import com.example.food.delivery.entity.Order;
import com.example.food.delivery.entity.Restaurant;
import com.example.food.delivery.entity.User;
import com.example.food.delivery.service.OrderService;
import com.example.food.delivery.service.RestaurantService;
import com.example.food.delivery.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/owner")
@RequiredArgsConstructor
public class OwnerDashboardController {

    private final UserService userService;
    private final RestaurantService restaurantService;
    private final OrderService orderService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User owner = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Logged-in user not found"));

        OwnerDashboardStatsDto stats = orderService.getOwnerDashboardStats(owner);
        List<Restaurant> restaurants = restaurantService.getRestaurantsByOwner(owner);
        List<Order> recentOrders = orderService.getOrdersByOwner(owner);
        if (recentOrders.size() > 5) {
            recentOrders = recentOrders.subList(0, 5);
        }

        model.addAttribute("owner", owner);
        model.addAttribute("stats", stats);
        model.addAttribute("restaurants", restaurants);
        model.addAttribute("recentOrders", recentOrders);

        return "owner/dashboard";
    }
}
