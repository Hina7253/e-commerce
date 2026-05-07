package com.ecommerce.controller;

import com.ecommerce.dto.DashboardStats;
import com.ecommerce.model.Order;
import com.ecommerce.model.Product;
import com.ecommerce.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:5173")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStats> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/dashboard/revenue-by-day")
    public ResponseEntity<Map<String, Object>> getRevenueByDay(@RequestParam int days) {
        return ResponseEntity.ok(adminService.getRevenueByDay(days));
    }

    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(adminService.getAllOrders(status, page));
    }

    @GetMapping("/products/low-stock")
    public ResponseEntity<List<Product>> getLowStockProducts() {
        return ResponseEntity.ok(adminService.getLowStockProducts());
    }

    @GetMapping("/top-products")
    public ResponseEntity<List<Product>> getTopProducts(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(adminService.getTopProducts(limit));
    }
}