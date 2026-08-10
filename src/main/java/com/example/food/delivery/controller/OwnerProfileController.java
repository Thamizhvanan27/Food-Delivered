package com.example.food.delivery.controller;

import com.example.food.delivery.entity.User;
import com.example.food.delivery.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/owner/profile")
@RequiredArgsConstructor
public class OwnerProfileController {

    private final UserService userService;

    private User getLoggedOwner(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @GetMapping
    public String showProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User owner = getLoggedOwner(userDetails);
        model.addAttribute("owner", owner);
        return "owner/profile";
    }

    @PostMapping("/update")
    public String updateProfile(@RequestParam String name,
                                @RequestParam String phone,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        User owner = getLoggedOwner(userDetails);
        try {
            userService.updateProfile(owner.getEmail(), name, phone);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/owner/profile";
    }

    @PostMapping("/password")
    public String changePassword(@RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        User owner = getLoggedOwner(userDetails);

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("passwordError", "New password and confirm password do not match!");
            return "redirect:/owner/profile";
        }

        try {
            userService.changePassword(owner.getEmail(), oldPassword, newPassword);
            redirectAttributes.addFlashAttribute("passwordSuccess", "Password updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("passwordError", e.getMessage());
        }
        return "redirect:/owner/profile";
    }
}
