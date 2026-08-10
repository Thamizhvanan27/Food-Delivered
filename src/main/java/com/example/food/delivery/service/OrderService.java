package com.example.food.delivery.service;

import com.example.food.delivery.dto.CartSummaryDto;
import com.example.food.delivery.dto.CheckoutDto;
import com.example.food.delivery.dto.DashboardStatsDto;
import com.example.food.delivery.entity.*;
import com.example.food.delivery.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartService cartService;
    private final AddressService addressService;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final FoodItemRepository foodItemRepository;

    @Transactional
    public Order placeOrder(User user, CheckoutDto checkoutDto) {
        CartSummaryDto cartSummary = cartService.getCartSummary(user, checkoutDto.getCouponCode());

        if (cartSummary.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot place order with an empty cart");
        }

        Address address = addressService.getAddressById(checkoutDto.getAddressId(), user);
        Restaurant restaurant = cartSummary.getRestaurant();

        String orderNumber = "ORD-" + System.currentTimeMillis() % 1000000 + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .user(user)
                .restaurant(restaurant)
                .deliveryAddress(address)
                .subtotal(cartSummary.getSubtotal())
                .deliveryFee(cartSummary.getDeliveryFee())
                .tax(cartSummary.getTax())
                .discountAmount(cartSummary.getDiscountAmount())
                .grandTotal(cartSummary.getGrandTotal())
                .paymentMethod(checkoutDto.getPaymentMethod())
                .paymentStatus(Order.PaymentStatus.PENDING)
                .orderStatus(Order.OrderStatus.PLACED)
                .couponCode(cartSummary.getCouponCode())
                .items(new ArrayList<>())
                .build();

        Order savedOrder = orderRepository.save(order);

        for (CartItem ci : cartSummary.getItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .order(savedOrder)
                    .foodItem(ci.getFoodItem())
                    .foodName(ci.getFoodItem().getName())
                    .price(ci.getFoodItem().getPrice())
                    .quantity(ci.getQuantity())
                    .build();
            orderItemRepository.save(orderItem);
            savedOrder.getItems().add(orderItem);
        }

        // Clear cart after placing order
        cartService.clearCart(user);

        return savedOrder;
    }

    public List<Order> getUserOrders(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public Order getOrderByIdAndUser(Long id, User user) {
        return orderRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Order not found or unauthorized"));
    }

    public Order getOrderByIdAdmin(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + id));
    }

    public List<Order> getAllOrdersAdmin() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByOrderStatus(status);
    }

    @Transactional
    public void updateOrderStatus(Long orderId, Order.OrderStatus status) {
        Order order = getOrderByIdAdmin(orderId);
        order.setOrderStatus(status);
        if (status == Order.OrderStatus.DELIVERED && order.getPaymentMethod() == Order.PaymentMethod.CASH_ON_DELIVERY) {
            order.setPaymentStatus(Order.PaymentStatus.PAID);
        }
        orderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(Long orderId, User user) {
        Order order = getOrderByIdAndUser(orderId, user);
        if (order.getOrderStatus() != Order.OrderStatus.PLACED && order.getOrderStatus() != Order.OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Order cannot be cancelled in status: " + order.getOrderStatus());
        }
        order.setOrderStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    public DashboardStatsDto getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalRestaurants = restaurantRepository.count();
        long totalFoodItems = foodItemRepository.count();
        long totalOrders = orderRepository.count();

        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long todayOrders = orderRepository.countByCreatedAtAfter(startOfDay);

        BigDecimal todayRevenue = orderRepository.sumGrandTotalByCreatedAtAfter(startOfDay);
        if (todayRevenue == null) todayRevenue = BigDecimal.ZERO;

        BigDecimal totalRevenue = orderRepository.sumTotalRevenue();
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        long pendingOrders = orderRepository.findByOrderStatus(Order.OrderStatus.PLACED).size() +
                orderRepository.findByOrderStatus(Order.OrderStatus.CONFIRMED).size() +
                orderRepository.findByOrderStatus(Order.OrderStatus.PREPARING).size() +
                orderRepository.findByOrderStatus(Order.OrderStatus.OUT_FOR_DELIVERY).size();

        long deliveredOrders = orderRepository.findByOrderStatus(Order.OrderStatus.DELIVERED).size();
        long cancelledOrders = orderRepository.findByOrderStatus(Order.OrderStatus.CANCELLED).size();

        return DashboardStatsDto.builder()
                .totalUsers(totalUsers)
                .totalRestaurants(totalRestaurants)
                .totalFoodItems(totalFoodItems)
                .totalOrders(totalOrders)
                .todayOrders(todayOrders)
                .todayRevenue(todayRevenue)
                .totalRevenue(totalRevenue)
                .pendingOrders(pendingOrders)
                .deliveredOrders(deliveredOrders)
                .cancelledOrders(cancelledOrders)
                .build();
    }

    public List<Order> getOrdersByOwner(User owner) {
        return orderRepository.findByRestaurantOwnerOrderByCreatedAtDesc(owner);
    }

    public Order getOrderByIdAndOwner(Long id, User owner) {
        return orderRepository.findByIdAndRestaurantOwner(id, owner)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Access denied or order not found for this restaurant owner."));
    }

    public List<Order> getOrdersByOwnerAndStatus(User owner, Order.OrderStatus status) {
        return orderRepository.findByRestaurantOwnerAndOrderStatusOrderByCreatedAtDesc(owner, status);
    }

    @Transactional
    public void updateOrderStatusByOwner(Long id, Order.OrderStatus status, User owner) {
        Order order = getOrderByIdAndOwner(id, owner);
        order.setOrderStatus(status);
        if (status == Order.OrderStatus.DELIVERED && order.getPaymentMethod() == Order.PaymentMethod.CASH_ON_DELIVERY) {
            order.setPaymentStatus(Order.PaymentStatus.PAID);
        }
        orderRepository.save(order);
    }

    public com.example.food.delivery.dto.OwnerDashboardStatsDto getOwnerDashboardStats(User owner) {
        long totalRestaurants = restaurantRepository.countByOwner(owner);
        long totalMenuItems = foodItemRepository.countByRestaurantOwner(owner);
        long totalOrders = orderRepository.countByRestaurantOwner(owner);

        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long todaysOrders = orderRepository.countByRestaurantOwnerAndCreatedAtAfter(owner, startOfDay);

        BigDecimal todaysRevenue = orderRepository.sumGrandTotalByOwnerAndCreatedAtAfter(owner, startOfDay);
        if (todaysRevenue == null) todaysRevenue = BigDecimal.ZERO;

        long pendingOrders = orderRepository.countByRestaurantOwnerAndOrderStatus(owner, Order.OrderStatus.PLACED) +
                orderRepository.countByRestaurantOwnerAndOrderStatus(owner, Order.OrderStatus.CONFIRMED) +
                orderRepository.countByRestaurantOwnerAndOrderStatus(owner, Order.OrderStatus.PREPARING) +
                orderRepository.countByRestaurantOwnerAndOrderStatus(owner, Order.OrderStatus.OUT_FOR_DELIVERY);

        long completedOrders = orderRepository.countByRestaurantOwnerAndOrderStatus(owner, Order.OrderStatus.DELIVERED);

        return com.example.food.delivery.dto.OwnerDashboardStatsDto.builder()
                .totalRestaurants(totalRestaurants)
                .totalMenuItems(totalMenuItems)
                .todaysOrders(todaysOrders)
                .pendingOrders(pendingOrders)
                .completedOrders(completedOrders)
                .todaysRevenue(todaysRevenue)
                .build();
    }
}
