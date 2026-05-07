package com.ecommerce.service;

import com.ecommerce.model.Order;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    public String createPaymentIntent(Order order) {
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(order.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue()) // Convert to cents
                    .setCurrency("usd")
                    .putMetadata("orderId", order.getId())
                    .putMetadata("userId", String.valueOf(order.getUser().getId()))
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);
            return paymentIntent.getId();
        } catch (StripeException e) {
            throw new RuntimeException("Failed to create payment intent: " + e.getMessage());
        }
    }

    public void confirmPayment(String paymentIntentId) {
        try