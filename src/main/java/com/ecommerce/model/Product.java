package com.ecommerce.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    private BigDecimal compareAtPrice; // Original price for discount display

    private Integer stockQuantity = 0;

    private String category;

    private String subCategory;

    @ElementCollection
    private List<String> images = new ArrayList<>();

    private String brand;

    private Double averageRating = 0.0;
    private Integer totalReviews = 0;
    private Integer totalSold = 0;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    private boolean active = true;

    @ElementCollection
    private List<String> tags = new ArrayList<>(); // For recommendations
}