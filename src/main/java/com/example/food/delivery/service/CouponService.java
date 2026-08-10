package com.example.food.delivery.service;

import com.example.food.delivery.entity.Coupon;
import com.example.food.delivery.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    public List<Coupon> getActiveCoupons() {
        return couponRepository.findByActiveTrue();
    }

    public Optional<Coupon> validateCoupon(String code, BigDecimal subtotal) {
        if (code == null || code.trim().isEmpty()) {
            return Optional.empty();
        }

        Optional<Coupon> couponOpt = couponRepository.findByCodeIgnoreCaseAndActiveTrue(code.trim());
        if (couponOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid coupon code");
        }

        Coupon coupon = couponOpt.get();
        if (coupon.getExpiryDate() != null && coupon.getExpiryDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Coupon code has expired");
        }

        if (coupon.getMinOrderAmount() != null && subtotal.compareTo(coupon.getMinOrderAmount()) < 0) {
            throw new IllegalArgumentException("Minimum order amount for coupon " + coupon.getCode() + " is ₹" + coupon.getMinOrderAmount());
        }

        return Optional.of(coupon);
    }

    public BigDecimal calculateDiscount(Coupon coupon, BigDecimal subtotal) {
        if (coupon == null || subtotal == null) return BigDecimal.ZERO;

        BigDecimal discount = BigDecimal.ZERO;

        if (coupon.getDiscountPercentage() != null && coupon.getDiscountPercentage() > 0) {
            discount = subtotal.multiply(BigDecimal.valueOf(coupon.getDiscountPercentage())).divide(BigDecimal.valueOf(100));
            if (coupon.getMaxDiscountAmount() != null && discount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
                discount = coupon.getMaxDiscountAmount();
            }
        } else if (coupon.getDiscountAmount() != null) {
            discount = coupon.getDiscountAmount();
        }

        return discount.compareTo(subtotal) > 0 ? subtotal : discount;
    }

    @Transactional
    public Coupon saveCoupon(Coupon coupon) {
        return couponRepository.save(coupon);
    }
}
