package com.example.food.delivery.controller;

import com.example.food.delivery.dto.CartSummaryDto;
import com.example.food.delivery.entity.User;
import com.example.food.delivery.service.CartService;
import com.example.food.delivery.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    @GetMapping
    public String viewCart(@RequestParam(value = "coupon", required = false) String couponCode,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        CartSummaryDto cartSummary = cartService.getCartSummary(user, couponCode);

        model.addAttribute("currentUser", user);
        model.addAttribute("cartSummary", cartSummary);
        model.addAttribute("cartItemCount", cartSummary.getTotalItemCount());
        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam("foodItemId") Long foodItemId,
                            @RequestParam(value = "quantity", defaultValue = "1") Integer quantity,
                            @AuthenticationPrincipal UserDetails userDetails,
                            RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();

        try {
            cartService.addToCart(user, foodItemId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Item added to cart!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String updateCart(@RequestParam("cartItemId") Long cartItemId,
                             @RequestParam("quantity") Integer quantity,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        cartService.updateQuantity(user, cartItemId, quantity);
        redirectAttributes.addFlashAttribute("successMessage", "Cart updated!");
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeFromCart(@RequestParam("cartItemId") Long cartItemId,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        cartService.removeItem(user, cartItemId);
        redirectAttributes.addFlashAttribute("successMessage", "Item removed from cart.");
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clearCart(@AuthenticationPrincipal UserDetails userDetails,
                            RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        cartService.clearCart(user);
        redirectAttributes.addFlashAttribute("successMessage", "Cart cleared.");
        return "redirect:/cart";
    }
}
