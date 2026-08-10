package com.example.food.delivery.controller;

import com.example.food.delivery.entity.Restaurant;
import com.example.food.delivery.entity.User;
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
@RequestMapping("/admin/restaurants")
@RequiredArgsConstructor
public class AdminRestaurantController {

    private final RestaurantService restaurantService;
    private final UserService userService;

    @GetMapping
    public String listRestaurants(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        model.addAttribute("currentUser", user);
        model.addAttribute("restaurants", restaurantService.getAllRestaurantsAdmin());
        return "admin/restaurants";
    }

    @GetMapping("/new")
    public String newRestaurantForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        model.addAttribute("currentUser", user);
        model.addAttribute("restaurant", new Restaurant());
        return "admin/restaurant-form";
    }

    @GetMapping("/edit/{id}")
    public String editRestaurantForm(@PathVariable Long id,
                                     @AuthenticationPrincipal UserDetails userDetails,
                                     Model model) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        model.addAttribute("currentUser", user);
        model.addAttribute("restaurant", restaurantService.getRestaurantById(id));
        return "admin/restaurant-form";
    }

    @PostMapping("/save")
    public String saveRestaurant(@Valid @ModelAttribute("restaurant") Restaurant restaurant,
                                 BindingResult result,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
            model.addAttribute("currentUser", user);
            return "admin/restaurant-form";
        }

        restaurantService.saveRestaurant(restaurant);
        redirectAttributes.addFlashAttribute("successMessage", "Restaurant saved successfully!");
        return "redirect:/admin/restaurants";
    }

    @PostMapping("/toggle/{id}")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        restaurantService.toggleActiveStatus(id);
        redirectAttributes.addFlashAttribute("successMessage", "Restaurant active status updated!");
        return "redirect:/admin/restaurants";
    }

    @PostMapping("/delete/{id}")
    public String deleteRestaurant(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            restaurantService.deleteRestaurant(id);
            redirectAttributes.addFlashAttribute("successMessage", "Restaurant deleted!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete restaurant with active orders or food items.");
        }
        return "redirect:/admin/restaurants";
    }
}
