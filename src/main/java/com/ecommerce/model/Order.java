package com.ecommerce.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    private BigDecimal subtotal;
    private BigDecimal shippingCost = BigDecimal.ZERO;
    private BigDecimal tax;
    private BigDecimal discount = BigDecimal.ZERO;
    private BigDecimal totalAmount;

    private String status = "PENDING"; // PENDING, PAID, PROCESSING, SHIPPED, DELIVERED, CANCELLED

    private String paymentIntentId;
    private String paymentStatus = "PENDING";

    // Delivery Information
    private String deliveryPartner;
    private String trackingId;
    private String trackingUrl;

    @Embedded
    private Address shippingAddress;

    @Embedded
    private Address billingAddress;

    private LocalDateTime orderDate = LocalDateTime.now();
    private LocalDateTime deliveredDate;

    private String notes;
}