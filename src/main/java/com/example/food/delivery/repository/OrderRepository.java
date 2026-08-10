package com.example.food.delivery.repository;

import com.example.food.delivery.entity.Order;
import com.example.food.delivery.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    Optional<Order> findByOrderNumber(String orderNumber);
    Optional<Order> findByIdAndUser(Long id, User user);
    List<Order> findAllByOrderByCreatedAtDesc();

    List<Order> findByOrderStatus(Order.OrderStatus status);

    long countByCreatedAtAfter(LocalDateTime dateTime);

    @Query("SELECT SUM(o.grandTotal) FROM Order o WHERE o.createdAt >= :dateTime AND o.orderStatus <> 'CANCELLED'")
    BigDecimal sumGrandTotalByCreatedAtAfter(LocalDateTime dateTime);

    @Query("SELECT SUM(o.grandTotal) FROM Order o WHERE o.orderStatus <> 'CANCELLED'")
    BigDecimal sumTotalRevenue();
}
