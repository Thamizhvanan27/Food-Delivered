package com.example.food.delivery.controller;

import com.example.food.delivery.dto.DashboardStatsDto;
import com.example.food.delivery.entity.User;
import com.example.food.delivery.service.OrderService;
import com.example.food.delivery.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final OrderService orderService;
    private final UserService userService;

    @GetMapping({"", "/dashboard"})
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        DashboardStatsDto stats = orderService.getDashboardStats();

        model.addAttribute("currentUser", user);
        model.addAttribute("stats", stats);
        model.addAttribute("recentOrders", orderService.getAllOrdersAdmin().stream().limit(5).toList());
        model.addAttribute("recentUsers", userService.getAllUsers().stream().limit(5).toList());
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String listUsers(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        model.addAttribute("currentUser", user);
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }
}
