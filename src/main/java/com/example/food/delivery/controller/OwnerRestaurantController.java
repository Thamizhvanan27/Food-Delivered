package com.example.food.delivery.controller;

import com.example.food.delivery.dto.OwnerRestaurantDto;
import com.example.food.delivery.entity.Restaurant;
import com.example.food.delivery.entity.User;
import com.example.food.delivery.service.FoodService;
import com.example.food.delivery.service.OrderService;
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
@RequestMapping("/owner/restaurants")
@RequiredArgsConstructor
public class OwnerRestaurantController {

    private final UserService userService;
    private final RestaurantService restaurantService;
    private final FoodService foodService;
    private final OrderService orderService;

    private User getLoggedOwner(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @GetMapping
    public String listRestaurants(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User owner = getLoggedOwner(userDetails);
        List<Restaurant> restaurants = restaurantService.getRestaurantsByOwner(owner);
        model.addAttribute("restaurants", restaurants);
        return "owner/restaurants";
    }

    @GetMapping("/new")
    public String showCreateForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (!model.containsAttribute("restaurantDto")) {
            model.addAttribute("restaurantDto", new OwnerRestaurantDto());
        }
        model.addAttribute("isEdit", false);
        return "owner/restaurant-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        User owner = getLoggedOwner(userDetails);
        Restaurant restaurant = restaurantService.getRestaurantByIdAndOwner(id, owner);

        OwnerRestaurantDto dto = new OwnerRestaurantDto();
        dto.setId(restaurant.getId());
        dto.setName(restaurant.getName());
        dto.setDescription(restaurant.getDescription());
        dto.setCuisine(restaurant.getCuisine());
        dto.setPhone(restaurant.getPhone());
        dto.setEmail(restaurant.getEmail());
        dto.setAddress(restaurant.getAddress());
        dto.setCity(restaurant.getCity());
        dto.setState(restaurant.getState());
        dto.setPincode(restaurant.getPincode());
        dto.setLandmark(restaurant.getLandmark());
        dto.setPriceRange(restaurant.getPriceRange());
        dto.setDeliveryTimeMinutes(restaurant.getDeliveryTimeMinutes());
        dto.setImageUrl(restaurant.getImageUrl());
        dto.setOpeningTime(restaurant.getOpeningTime());
        dto.setClosingTime(restaurant.getClosingTime());
        dto.setOperationalStatus(restaurant.getOperationalStatus());
        dto.setManualClosed(restaurant.getManualClosed());

        model.addAttribute("restaurantDto", dto);
        model.addAttribute("isEdit", true);
        return "owner/restaurant-form";
    }

    @PostMapping("/save")
    public String saveRestaurant(@Valid @ModelAttribute("restaurantDto") OwnerRestaurantDto dto,
                                 BindingResult bindingResult,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        User owner = getLoggedOwner(userDetails);

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", dto.getId() != null);
            return "owner/restaurant-form";
        }

        try {
            restaurantService.saveOwnerRestaurant(dto, owner);
            redirectAttributes.addFlashAttribute("success", "Restaurant saved successfully!");
            return "redirect:/owner/restaurants";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("isEdit", dto.getId() != null);
            return "owner/restaurant-form";
        }
    }

    @PostMapping("/toggle-status/{id}")
    public String toggleStatus(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        User owner = getLoggedOwner(userDetails);
        try {
            restaurantService.toggleOperationalStatus(id, owner);
            redirectAttributes.addFlashAttribute("success", "Restaurant operational status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/owner/restaurants";
    }
}
