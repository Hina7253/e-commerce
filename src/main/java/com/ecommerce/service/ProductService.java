package com.ecommerce.service;

import com.ecommerce.dto.ProductDto;
import com.ecommerce.model.Product;
import com.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public Page<Product> getProducts(int page, int size, String category, String search, BigDecimal minPrice, BigDecimal maxPrice) {
        PageRequest pageRequest = PageRequest.of(page, size);

        if (search != null && !search.isEmpty()) {
            return productRepository.findByNameContainingIgnoreCase(search, pageRequest);
        }
        if (category != null && !category.isEmpty()) {
            return productRepository.findByCategory(category, pageRequest);
        }
        if (minPrice != null && maxPrice != null) {
            return productRepository.findByPriceRange(minPrice, maxPrice, pageRequest);
        }

        return productRepository.findAll(pageRequest);
    }

    public Product getProductById(String id) {
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public Product createProduct(ProductDto dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setCategory(dto.getCategory());
        product.setImages(dto.getImages());
        return productRepository.save(product);
    }

    public Product updateProduct(String id, ProductDto dto) {
        Product product = getProductById(id);
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setCategory(dto.getCategory());
        product.setImages(dto.getImages());
        return productRepository.save(product);
    }

    public void deleteProduct(String id) {
        productRepository.deleteById(id);
    }

    public Product updateStock(String id, int quantity) {
        Product product = getProductById(id);
        product.setStockQuantity(quantity);
        return productRepository.save(product);
    }
}