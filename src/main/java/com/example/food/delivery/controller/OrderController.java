package com.example.food.delivery.controller;

import com.example.food.delivery.entity.Order;
import com.example.food.delivery.entity.User;
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

import java.util.List;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final CartService cartService;

    @GetMapping
    public String myOrders(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        List<Order> orders = orderService.getUserOrders(user);

        model.addAttribute("currentUser", user);
        model.addAttribute("orders", orders);
        model.addAttribute("cartItemCount", cartService.getCartSummary(user, null).getTotalItemCount());
        return "my-orders";
    }

    @GetMapping("/{id}")
    public String orderDetails(@PathVariable Long id,
                               @RequestParam(value = "success", required = false) Boolean success,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        Order order = orderService.getOrderByIdAndUser(id, user);

        model.addAttribute("currentUser", user);
        model.addAttribute("order", order);
        model.addAttribute("isNewOrder", Boolean.TRUE.equals(success));
        model.addAttribute("cartItemCount", cartService.getCartSummary(user, null).getTotalItemCount());
        return "order-details";
    }

    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        try {
            orderService.cancelOrder(id, user);
            redirectAttributes.addFlashAttribute("successMessage", "Order has been cancelled successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/orders/" + id;
    }
}
