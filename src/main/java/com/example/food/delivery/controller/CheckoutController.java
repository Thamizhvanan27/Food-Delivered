package com.example.food.delivery.controller;

import com.example.food.delivery.dto.AddressDto;
import com.example.food.delivery.dto.CartSummaryDto;
import com.example.food.delivery.dto.CheckoutDto;
import com.example.food.delivery.entity.Address;
import com.example.food.delivery.entity.Order;
import com.example.food.delivery.entity.User;
import com.example.food.delivery.service.AddressService;
import com.example.food.delivery.service.CartService;
import com.example.food.delivery.service.OrderService;
import com.example.food.delivery.service.StripeService;
import com.example.food.delivery.service.UserService;
import com.stripe.model.checkout.Session;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CartService cartService;
    private final AddressService addressService;
    private final OrderService orderService;
    private final UserService userService;
    private final StripeService stripeService;

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

        List<Address> addresses = addressService.getUserAddresses(user);

        if (!model.containsAttribute("checkoutDto")) {
            CheckoutDto dto = new CheckoutDto();
            dto.setCouponCode(cartSummary.getCouponCode());
            dto.setPaymentMethod(Order.PaymentMethod.ONLINE);
            if (!addresses.isEmpty()) {
                Address defaultAddress = addresses.stream()
                        .filter(Address::isDefault)
                        .findFirst()
                        .orElse(addresses.get(0));
                dto.setAddressId(defaultAddress.getId());
            }
            model.addAttribute("checkoutDto", dto);
        }

        model.addAttribute("currentUser", user);
        model.addAttribute("cartSummary", cartSummary);
        model.addAttribute("addresses", addresses);
        model.addAttribute("newAddressDto", new AddressDto());
        model.addAttribute("cartItemCount", cartSummary.getTotalItemCount());
        return "checkout";
    }

    @PostMapping("/place-order")
    public String placeOrder(@Valid @ModelAttribute("checkoutDto") CheckoutDto checkoutDto,
                             BindingResult result,
                             @AuthenticationPrincipal UserDetails userDetails,
                             HttpServletRequest request,
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

            if (checkoutDto.getPaymentMethod() == Order.PaymentMethod.ONLINE) {
                String hostHeader = request.getHeader("Host");
                String forwardedProto = request.getHeader("X-Forwarded-Proto");
                String scheme = forwardedProto != null ? forwardedProto : request.getScheme();
                String baseUrl = scheme + "://" + (hostHeader != null ? hostHeader : request.getServerName() + ":" + request.getServerPort());
                String successUrl = baseUrl + "/checkout/stripe/success?session_id={CHECKOUT_SESSION_ID}&order_id=" + order.getId();
                String cancelUrl = baseUrl + "/checkout/stripe/cancel?order_id=" + order.getId();

                try {
                    Session session = stripeService.createCheckoutSession(order, successUrl, cancelUrl);
                    orderService.saveStripeSessionId(order.getId(), session.getId());
                    return "redirect:" + session.getUrl();
                } catch (Exception e) {
                    log.error("Failed to create Stripe session for order ID: {}", order.getId(), e);
                    orderService.updateStripePaymentDetails(order.getId(), null, Order.PaymentStatus.FAILED);
                    redirectAttributes.addFlashAttribute("errorMessage", "Failed to initiate Stripe payment: " + e.getMessage());
                    return "redirect:/orders/" + order.getId();
                }
            }

            return "redirect:/orders/" + order.getId() + "?success=true";
        } catch (Exception e) {
            log.error("Error placing order: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/checkout";
        }
    }

    @GetMapping("/stripe/success")
    public String stripeSuccess(@RequestParam("session_id") String sessionId,
                               @RequestParam("order_id") Long orderId,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        Order order = orderService.getOrderByIdAndUser(orderId, user);
        try {
            Session session = stripeService.retrieveSession(sessionId);
            if ("paid".equalsIgnoreCase(session.getPaymentStatus())) {
                orderService.updateStripePaymentDetails(order.getId(), session.getPaymentIntent(), Order.PaymentStatus.PAID);
                return "redirect:/orders/" + order.getId() + "?success=true";
            } else {
                orderService.updateStripePaymentDetails(order.getId(), session.getPaymentIntent(), Order.PaymentStatus.FAILED);
                redirectAttributes.addFlashAttribute("errorMessage", "Stripe payment was not completed.");
                return "redirect:/orders/" + order.getId();
            }
        } catch (Exception e) {
            log.error("Error retrieving Stripe session: {}", sessionId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error verifying Stripe session: " + e.getMessage());
            return "redirect:/orders/" + order.getId();
        }
    }

    @GetMapping("/stripe/cancel")
    public String stripeCancel(@RequestParam("order_id") Long orderId,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        Order order = orderService.getOrderByIdAndUser(orderId, user);
        orderService.updateStripePaymentDetails(order.getId(), null, Order.PaymentStatus.FAILED);
        redirectAttributes.addFlashAttribute("errorMessage", "Stripe payment was cancelled.");
        return "redirect:/orders/" + order.getId();
    }
}
