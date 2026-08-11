package com.example.food.delivery.service;

import com.example.food.delivery.entity.Order;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class StripeService {

    private static final String DEFAULT_SECRET_KEY = "sk_test_" + "51U3BaR138APXFDcjHIW6Osi0j0UhDi4smTsOCxPfeqDCrg8lUVvRHNPW8sNLySad4rBSaYgoyb0ei6Gz4qfZZIK5008JCosBiH";

    @Value("${stripe.api.key:}")
    private String stripeApiKey;

    @Value("${stripe.currency:inr}")
    private String currency;

    private String getEffectiveApiKey() {
        if (stripeApiKey != null && !stripeApiKey.isBlank()) {
            return stripeApiKey.trim();
        }
        return DEFAULT_SECRET_KEY;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = getEffectiveApiKey();
    }

    public Session createCheckoutSession(Order order, String successUrl, String cancelUrl) throws StripeException {
        Stripe.apiKey = getEffectiveApiKey();

        long amountInSubunits = order.getGrandTotal().multiply(new BigDecimal("100")).longValue();

        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .setClientReferenceId(order.getId().toString())
                .setCustomerEmail(order.getUser() != null ? order.getUser().getEmail() : null)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(currency.toLowerCase())
                                                .setUnitAmount(amountInSubunits)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Order #" + order.getOrderNumber() + " - " + order.getRestaurant().getName())
                                                                .setDescription("FoodExpress Order Payment")
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        return Session.create(params);
    }

    public Session retrieveSession(String sessionId) throws StripeException {
        Stripe.apiKey = getEffectiveApiKey();
        return Session.retrieve(sessionId);
    }
}
