package com.example.food.delivery.controller;

import com.example.food.delivery.entity.Order;
import com.example.food.delivery.entity.User;
import com.example.food.delivery.service.CartService;
import com.example.food.delivery.service.OrderService;
import com.example.food.delivery.service.StripeService;
import com.example.food.delivery.service.UserService;
import com.stripe.model.checkout.Session;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final CartService cartService;
    private final StripeService stripeService;

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

    @PostMapping("/{id}/pay")
    public String payOrderWithStripe(@PathVariable Long id,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    HttpServletRequest request,
                                    RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        Order order = orderService.getOrderByIdAndUser(id, user);

        if (order.getPaymentStatus() == Order.PaymentStatus.PAID) {
            redirectAttributes.addFlashAttribute("infoMessage", "Order is already paid.");
            return "redirect:/orders/" + id;
        }

        try {
            String hostHeader = request.getHeader("Host");
            String forwardedProto = request.getHeader("X-Forwarded-Proto");
            String scheme = forwardedProto != null ? forwardedProto : request.getScheme();
            String baseUrl = scheme + "://" + (hostHeader != null ? hostHeader : request.getServerName() + ":" + request.getServerPort());
            String successUrl = baseUrl + "/checkout/stripe/success?session_id={CHECKOUT_SESSION_ID}&order_id=" + order.getId();
            String cancelUrl = baseUrl + "/checkout/stripe/cancel?order_id=" + order.getId();

            Session session = stripeService.createCheckoutSession(order, successUrl, cancelUrl);
            orderService.saveStripeSessionId(order.getId(), session.getId());
            return "redirect:" + session.getUrl();
        } catch (Exception e) {
            log.error("Failed to open Stripe payment page for order: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to open Stripe payment page: " + e.getMessage());
            return "redirect:/orders/" + id;
        }
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
