package com.ecommerce.service;

import com.ecommerce.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOrderConfirmation(String email, Order order) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Order Confirmation - #" + order.getId());
        message.setText(String.format(
                "Dear Customer,\n\nYour order #%s has been received.\nTotal Amount: $%.2f\n\nWe'll notify you once it's shipped.\n\nThank you for shopping with us!",
                order.getId(), order.getTotalAmount()
        ));
        mailSender.send(message);
    }

    public void sendOrderStatusUpdate(String email, Order order) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Order Update - #" + order.getId());
        message.setText(String.format(
                "Dear Customer,\n\nYour order #%s status has been updated to: %s\n\nTracking ID: %s\n\nThank you!",
                order.getId(), order.getStatus(), order.getTrackingId() != null ? order.getTrackingId() : "N/A"
        ));
        mailSender.send(message);
    }

    public void sendOrderCancellation(String email, Order order) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Order Cancelled - #" + order.getId());
        message.setText(String.format(
                "Dear Customer,\n\nYour order #%s has been cancelled.\n\nRefund will be processed within 5-7 business days.\n\nThank you!",
                order.getId()
        ));
        mailSender.send(message);
    }
}