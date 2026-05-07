package com.ecommerce.service;

import com.ecommerce.dto.OrderRequest;
import com.ecommerce.model.*;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private DeliveryService deliveryService;

    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
    }

    @Transactional
    public Order createOrder(OrderRequest request) {
        User user = getCurrentUser();
        Cart cart = user.getCart();

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Order order = new Order();
        order.setUser(user);
        order.setId(UUID.randomUUID().toString());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");

        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setProductName(cartItem.getProduct().getName());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtTime(cartItem.getProduct().getPrice());
            orderItem.setTotal(cartItem.getProduct().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));

            order.getItems().add(orderItem);
            subtotal = subtotal.add(orderItem.getTotal());

            // Update stock
            Product product = cartItem.getProduct();
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }

        order.setSubtotal(subtotal);
        order.setShippingCost(BigDecimal.valueOf(50)); // Fixed shipping
        order.setTax(subtotal.multiply(BigDecimal.valueOf(0.1))); // 10% tax
        order.setTotalAmount(subtotal.add(order.getShippingCost()).add(order.getTax()));

        order.setShippingAddress(request.getShippingAddress());
        order.setBillingAddress(request.getBillingAddress() != null ? request.getBillingAddress() : request.getShippingAddress());

        Order savedOrder = orderRepository.save(order);

        // Create payment intent
        String paymentIntentId = paymentService.createPaymentIntent(savedOrder);
        savedOrder.setPaymentIntentId(paymentIntentId);

        // Clear cart
        cart.getItems().clear();
        cart.setTotalPrice(BigDecimal.ZERO);
        cartRepository.save(cart);

        // Send notification
        notificationService.sendOrderConfirmation(user.getEmail(), savedOrder);

        return orderRepository.save(savedOrder);
    }

    public Order updateOrderStatus(String orderId, String status) {
        Order order = getOrder(orderId);
        order.setStatus(status);

        if (status.equals("CONFIRMED")) {
            // Integrate with delivery partner
            Map<String, Object> deliveryInfo = deliveryService.createShipment(order);
            order.setDeliveryPartner((String) deliveryInfo.get("partner"));
            order.setTrackingId((String) deliveryInfo.get("trackingId"));
            order.setTrackingUrl((String) deliveryInfo.get("trackingUrl"));
        }

        if (status.equals("DELIVERED")) {
            order.setDeliveredDate(LocalDateTime.now());
        }

        notificationService.sendOrderStatusUpdate(order.getUser().getEmail(), order);
        return orderRepository.save(order);
    }

    public Order getOrder(String orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public List<Order> getCurrentUserOrders() {
        User user = getCurrentUser();
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }

    @Transactional
    public Order cancelOrder(String orderId) {
        Order order = getOrder(orderId);

        if (!order.getStatus().equals("PENDING") && !order.getStatus().equals("PAID")) {
            throw new RuntimeException("Order cannot be cancelled");
        }

        order.setStatus("CANCELLED");

        // Restore stock
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        // Process refund if payment was made
        if (order.getPaymentStatus().equals("PAID")) {
            paymentService.refundPayment(order.getPaymentIntentId());
        }

        notificationService.sendOrderCancellation(order.getUser().getEmail(), order);
        return orderRepository.save(order);
    }

    public Map<String, Object> trackOrder(String orderId) {
        Order order = getOrder(orderId);
        Map<String, Object> trackingInfo = new HashMap<>();
        trackingInfo.put("status", order.getStatus());
        trackingInfo.put("trackingId", order.getTrackingId());
        trackingInfo.put("trackingUrl", order.getTrackingUrl());
        trackingInfo.put("estimatedDelivery", order.getOrderDate().plusDays(5));

        if (order.getDeliveryPartner() != null) {
            Map<String, Object> liveTracking = deliveryService.getTrackingStatus(order.getTrackingId());
            trackingInfo.put("liveStatus", liveTracking);
        }

        return trackingInfo;
    }
}
