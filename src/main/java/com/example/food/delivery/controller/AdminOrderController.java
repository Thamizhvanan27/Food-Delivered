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
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;
    private final UserService userService;

    @GetMapping
    public String listOrders(@RequestParam(value = "status", required = false) String statusStr,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        model.addAttribute("currentUser", user);

        List<Order> orders;
        if (statusStr != null && !statusStr.trim().isEmpty() && !statusStr.equalsIgnoreCase("ALL")) {
            try {
                Order.OrderStatus status = Order.OrderStatus.valueOf(statusStr.toUpperCase());
                orders = orderService.getOrdersByStatus(status);
            } catch (Exception e) {
                orders = orderService.getAllOrdersAdmin();
            }
        } else {
            orders = orderService.getAllOrdersAdmin();
        }

        model.addAttribute("orders", orders);
        model.addAttribute("statuses", Order.OrderStatus.values());
        model.addAttribute("currentStatusFilter", statusStr);
        return "admin/orders";
    }

    @GetMapping("/{id}")
    public String orderDetails(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        model.addAttribute("currentUser", user);
        model.addAttribute("order", orderService.getOrderByIdAdmin(id));
        model.addAttribute("statuses", Order.OrderStatus.values());
        return "admin/order-details";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam("status") Order.OrderStatus status,
                               RedirectAttributes redirectAttributes) {
        orderService.updateOrderStatus(id, status);
        redirectAttributes.addFlashAttribute("successMessage", "Order status updated to " + status);
        return "redirect:/admin/orders/" + id;
    }
}
