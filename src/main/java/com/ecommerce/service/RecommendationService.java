package com.ecommerce.service;

import com.ecommerce.model.Order;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername()).orElse(null);
    }

    public List<Product> getRecommendations(int limit) {
        // Hybrid recommendation: combine popularity + user history
        User user = getCurrentUser();

        if (user == null) {
            // Guest users: get trending products
            return productRepository.findTopSellingOverall(PageRequest.of(0, limit));
        }

        // Get user's purchased categories
        List<Order> userOrders = orderRepository.findByUserOrderByOrderDateDesc(user);
        Set<String> purchasedCategories = new HashSet<>();
        Set<String> purchasedProductIds = new HashSet<>();

        for (Order order : userOrders) {
            order.getItems().forEach(item -> {
                purchasedCategories.add(item.getProduct().getCategory());
                purchasedProductIds.add(item.getProduct().getId());
            });
        }

        List<Product> recommendations = new ArrayList<>();

        // 1. Recommend products from purchased categories not yet bought
        if (!purchasedCategories.isEmpty()) {
            List<Product> categoryProducts = productRepository.findTopSellingByCategories(
                    new ArrayList<>(purchasedCategories), PageRequest.of(0, limit));

            categoryProducts.stream()
                    .filter(p -> !purchasedProductIds.contains(p.getId()))
                    .forEach(recommendations::add);
        }

        // 2. Fill remaining with highly rated products
        if (recommendations.size() < limit) {
            List<Product> topRated = productRepository.findTopSellingOverall(PageRequest.of(0, limit * 2));
            topRated.stream()
                    .filter(p -> !purchasedProductIds.contains(p.getId()))
                    .filter(p -> !recommendations.contains(p))
                    .limit(limit - recommendations.size())
                    .forEach(recommendations::add);
        }

        return recommendations.stream().limit(limit).collect(Collectors.toList());
    }

    public List<Product> getSimilarProducts(String productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return new ArrayList<>();

        // Find products in same category
        return productRepository.findByCategory(product.getCategory(), PageRequest.of(0, 10))
                .stream()
                .filter(p -> !p.getId().equals(productId))
                .limit(8)
                .collect(Collectors.toList());
    }

    public List<Product> getPersonalizedRecommendations(int limit) {
        return getRecommendations(limit);
    }
}