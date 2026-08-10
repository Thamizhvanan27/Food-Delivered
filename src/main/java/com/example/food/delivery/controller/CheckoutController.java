package com.example.food.delivery.controller;

import com.example.food.delivery.dto.AddressDto;
import com.example.food.delivery.dto.CartSummaryDto;
import com.example.food.delivery.dto.CheckoutDto;
import com.example.food.delivery.entity.Order;
import com.example.food.delivery.entity.User;
import com.example.food.delivery.service.AddressService;
import com.example.food.delivery.service.CartService;
import com.example.food.delivery.service.OrderService;
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
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CartService cartService;
    private final AddressService addressService;
    private final OrderService orderService;
    private final UserService userService;

    @GetMapping
    public String checkoutPage(@RequestParam(value = "coupon", required = false) String couponCode,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        CartSummaryDto cartSummary = cartService.getCartSummary(user, couponCode);

        if (cartSummary.getItems().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Your cart is empty! Please add items before checking out.");
            return "redirect:/restaurants";
        }

        if (!model.containsAttribute("checkoutDto")) {
            CheckoutDto dto = new CheckoutDto();
            dto.setCouponCode(cartSummary.getCouponCode());
            model.addAttribute("checkoutDto", dto);
        }

        model.addAttribute("currentUser", user);
        model.addAttribute("cartSummary", cartSummary);
        model.addAttribute("addresses", addressService.getUserAddresses(user));
        model.addAttribute("newAddressDto", new AddressDto());
        model.addAttribute("cartItemCount", cartSummary.getTotalItemCount());
        return "checkout";
    }

    @PostMapping("/place-order")
    public String placeOrder(@Valid @ModelAttribute("checkoutDto") CheckoutDto checkoutDto,
                             BindingResult result,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();

        if (result.hasErrors()) {
            CartSummaryDto cartSummary = cartService.getCartSummary(user, checkoutDto.getCouponCode());
            model.addAttribute("currentUser", user);
            model.addAttribute("cartSummary", cartSummary);
            model.addAttribute("addresses", addressService.getUserAddresses(user));
            model.addAttribute("newAddressDto", new AddressDto());
            model.addAttribute("cartItemCount", cartSummary.getTotalItemCount());
            return "checkout";
        }

        try {
            Order order = orderService.placeOrder(user, checkoutDto);
            return "redirect:/orders/" + order.getId() + "?success=true";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/checkout";
        }
    }
}
