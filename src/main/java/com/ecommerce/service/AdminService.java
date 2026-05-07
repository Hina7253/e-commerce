package com.ecommerce.service;

import com.ecommerce.dto.DashboardStats;
import com.ecommerce.model.Order;
import com.ecommerce.model.Product;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AdminService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();
        stats.setTotalRevenue(orderRepository.getTodayRevenue());
        stats.setTodayOrders(orderRepository.countTodayOrders());
        stats.setTotalUsers(userRepository.count());
        stats.setTotalProducts(productRepository.count());
        stats.setPendingOrders(orderRepository.findByStatus("PENDING").size());
        stats.setLowStockProducts(productRepository.findByStockQuantityLessThan(10).size());
        return stats;
    }

    public Map<String, Object> getRevenueByDay(int days) {
        Map<String, Object> result = new HashMap<>();
        List<String> dates = new ArrayList<>();
        List<BigDecimal> revenues = new ArrayList<>();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            dates.add(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
            // In real implementation, query from database
            revenues.add(BigDecimal.valueOf(Math.random() * 10000));
        }

        result.put("dates", dates);
        result.put("revenues", revenues);
        return result;
    }

    public List<Order> getAllOrders(String status, int page) {
        if (status != null && !status.isEmpty()) {
            return orderRepository.findByStatus(status);
        }
        return orderRepository.findRecentOrders(PageRequest.of(page, 20));
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findByStockQuantityLessThan(10);
    }

    public List<Product> getTopProducts(int limit) {
        return productRepository.findTopSellingOverall(PageRequest.of(0, limit));
    }
}