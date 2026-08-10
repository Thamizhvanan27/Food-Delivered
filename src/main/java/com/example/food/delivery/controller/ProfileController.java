package com.example.food.delivery.controller;

import com.example.food.delivery.entity.User;
import com.example.food.delivery.service.AddressService;
import com.example.food.delivery.service.CartService;
import com.example.food.delivery.service.OrderService;
import com.example.food.delivery.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final AddressService addressService;
    private final OrderService orderService;
    private final CartService cartService;

    @GetMapping
    public String viewProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();

        model.addAttribute("currentUser", user);
        model.addAttribute("addresses", addressService.getUserAddresses(user));
        model.addAttribute("orders", orderService.getUserOrders(user));
        model.addAttribute("cartItemCount", cartService.getCartSummary(user, null).getTotalItemCount());
        return "profile";
    }

    @PostMapping("/update")
    public String updateProfile(@RequestParam("name") String name,
                                @RequestParam("phone") String phone,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        try {
            userService.updateProfile(userDetails.getUsername(), name, phone);
            redirectAttributes.addFlashAttribute("successMessage", "Profile details updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        try {
            userService.changePassword(userDetails.getUsername(), oldPassword, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Password updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/profile";
    }
}
