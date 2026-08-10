package com.example.food.delivery.controller;

import com.example.food.delivery.dto.AddressDto;
import com.example.food.delivery.entity.User;
import com.example.food.delivery.service.AddressService;
import com.example.food.delivery.service.CartService;
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
@RequestMapping("/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;
    private final UserService userService;
    private final CartService cartService;

    @GetMapping
    public String viewAddresses(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();

        if (!model.containsAttribute("addressDto")) {
            model.addAttribute("addressDto", new AddressDto());
        }

        model.addAttribute("currentUser", user);
        model.addAttribute("addresses", addressService.getUserAddresses(user));
        model.addAttribute("cartItemCount", cartService.getCartSummary(user, null).getTotalItemCount());
        return "addresses";
    }

    @PostMapping("/save")
    public String saveAddress(@Valid @ModelAttribute("addressDto") AddressDto addressDto,
                              BindingResult result,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Validation error: Please fill all required address fields correctly.");
            return "redirect:/addresses";
        }

        try {
            addressService.saveAddress(user, addressDto);
            redirectAttributes.addFlashAttribute("successMessage", "Address saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/addresses";
    }

    @PostMapping("/delete/{id}")
    public String deleteAddress(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        try {
            addressService.deleteAddress(id, user);
            redirectAttributes.addFlashAttribute("successMessage", "Address deleted!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/addresses";
    }
}
