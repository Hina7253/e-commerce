package com.ecommerce.controller;

import com.ecommerce.model.Product;
import com.ecommerce.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(origins = "http://localhost:5173")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @GetMapping
    public ResponseEntity<List<Product>> getRecommendations(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(recommendationService.getRecommendations(limit));
    }

    @GetMapping("/similar/{productId}")
    public ResponseEntity<List<Product>> getSimilarProducts(@PathVariable String productId) {
        return ResponseEntity.ok(recommendationService.getSimilarProducts(productId));
    }

    @GetMapping("/personalized")
    public ResponseEntity<List<Product>> getPersonalizedRecommendations(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(recommendationService.getPersonalizedRecommendations(limit));
    }
}