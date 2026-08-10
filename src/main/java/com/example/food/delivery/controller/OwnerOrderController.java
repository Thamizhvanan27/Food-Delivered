package com.example.food.delivery.controller;

import com.example.food.delivery.entity.Order;
import com.example.food.delivery.entity.User;
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
@RequestMapping("/owner/orders")
@RequiredArgsConstructor
public class OwnerOrderController {

    private final UserService userService;
    private final OrderService orderService;

    private User getLoggedOwner(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @GetMapping
    public String listOrders(@RequestParam(required = false) String status,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        User owner = getLoggedOwner(userDetails);
        List<Order> orders;

        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
            try {
                Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
                orders = orderService.getOrdersByOwnerAndStatus(owner, orderStatus);
            } catch (Exception e) {
                orders = orderService.getOrdersByOwner(owner);
            }
        } else {
            orders = orderService.getOrdersByOwner(owner);
        }

        model.addAttribute("orders", orders);
        model.addAttribute("currentStatus", status != null ? status : "ALL");
        model.addAttribute("statuses", Order.OrderStatus.values());
        return "owner/orders";
    }

    @GetMapping("/{id}")
    public String orderDetails(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        User owner = getLoggedOwner(userDetails);
        Order order = orderService.getOrderByIdAndOwner(id, owner);
        model.addAttribute("order", order);
        model.addAttribute("statuses", Order.OrderStatus.values());
        return "owner/order-details";
    }

    @PostMapping("/{id}/status")
    public String updateOrderStatus(@PathVariable Long id,
                                    @RequestParam Order.OrderStatus status,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {
        User owner = getLoggedOwner(userDetails);
        try {
            orderService.updateOrderStatusByOwner(id, status, owner);
            redirectAttributes.addFlashAttribute("success", "Order status updated to " + status + "!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/owner/orders/" + id;
    }
}
