package com.example.food.delivery.service;

import com.example.food.delivery.dto.CartSummaryDto;
import com.example.food.delivery.entity.*;
import com.example.food.delivery.repository.CartItemRepository;
import com.example.food.delivery.repository.CartRepository;
import com.example.food.delivery.repository.FoodItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final FoodItemRepository foodItemRepository;
    private final CouponService couponService;

    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
    }

    @Transactional
    public void addToCart(User user, Long foodItemId, Integer quantity) {
        if (quantity == null || quantity <= 0) quantity = 1;

        FoodItem foodItem = foodItemRepository.findById(foodItemId)
                .orElseThrow(() -> new IllegalArgumentException("Food item not found"));

        if (!foodItem.isAvailable()) {
            throw new IllegalArgumentException("Selected item is currently unavailable");
        }

        Cart cart = getOrCreateCart(user);

        // Check if cart has items from another restaurant
        if (!cart.getItems().isEmpty()) {
            Restaurant currentRestaurant = cart.getItems().get(0).getFoodItem().getRestaurant();
            if (!currentRestaurant.getId().equals(foodItem.getRestaurant().getId())) {
                throw new IllegalStateException("Your cart contains items from " + currentRestaurant.getName() + 
                        ". Clear your cart to add items from " + foodItem.getRestaurant().getName() + ".");
            }
        }

        Optional<CartItem> existingItem = cartItemRepository.findByCartAndFoodItem(cart, foodItem);

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .foodItem(foodItem)
                    .quantity(quantity)
                    .build();
            cartItemRepository.save(newItem);
            cart.getItems().add(newItem);
        }
    }

    @Transactional
    public void updateQuantity(User user, Long cartItemId, Integer quantity) {
        Cart cart = getOrCreateCart(user);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Unauthorized cart access");
        }

        if (quantity == null || quantity <= 0) {
            cart.getItems().remove(cartItem);
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }
    }

    @Transactional
    public void removeItem(User user, Long cartItemId) {
        updateQuantity(user, cartItemId, 0);
    }

    @Transactional
    public void clearCart(User user) {
        Cart cart = getOrCreateCart(user);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    public CartSummaryDto getCartSummary(User user, String couponCode) {
        Cart cart = getOrCreateCart(user);
        
        BigDecimal subtotal = BigDecimal.ZERO;
        int totalItemCount = 0;
        Restaurant restaurant = null;

        if (!cart.getItems().isEmpty()) {
            restaurant = cart.getItems().get(0).getFoodItem().getRestaurant();
            for (CartItem item : cart.getItems()) {
                BigDecimal itemTotal = item.getFoodItem().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                subtotal = subtotal.add(itemTotal);
                totalItemCount += item.getQuantity();
            }
        }

        BigDecimal deliveryFee = cart.getItems().isEmpty() ? BigDecimal.ZERO : new BigDecimal("40.00");
        BigDecimal tax = subtotal.multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP); // 5% tax

        BigDecimal discount = BigDecimal.ZERO;
        String appliedCoupon = null;

        if (couponCode != null && !couponCode.trim().isEmpty()) {
            try {
                Optional<Coupon> couponOpt = couponService.validateCoupon(couponCode, subtotal);
                if (couponOpt.isPresent()) {
                    discount = couponService.calculateDiscount(couponOpt.get(), subtotal);
                    appliedCoupon = couponOpt.get().getCode();
                }
            } catch (Exception e) {
                // Ignore invalid coupon during summary display, or let caller catch
            }
        }

        BigDecimal grandTotal = subtotal.add(deliveryFee).add(tax).subtract(discount);
        if (grandTotal.compareTo(BigDecimal.ZERO) < 0) grandTotal = BigDecimal.ZERO;

        return CartSummaryDto.builder()
                .items(cart.getItems())
                .restaurant(restaurant)
                .subtotal(subtotal)
                .deliveryFee(deliveryFee)
                .tax(tax)
                .discountAmount(discount)
                .couponCode(appliedCoupon)
                .grandTotal(grandTotal)
                .totalItemCount(totalItemCount)
                .build();
    }
}
