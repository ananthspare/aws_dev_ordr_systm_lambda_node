package com.ecommerce.service;

import com.ecommerce.dto.ProductDto;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * Create a new product
     */
    public ProductDto.Response createProduct(ProductDto.CreateRequest request) {
        log.info("Creating new product: {}", request.getName());

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQty(request.getStockQty())
                .imageUrl(request.getImageUrl())
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {}", savedProduct.getProductId());

        return mapToResponse(savedProduct);
    }

    /**
     * Get product by ID
     */
    @Transactional(readOnly = true)
    public ProductDto.Response getProductById(Long productId) {
        log.debug("Fetching product with ID: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        return mapToResponse(product);
    }

    /**
     * Get all products with pagination
     */
    @Transactional(readOnly = true)
    public Page<ProductDto.Response> getAllProducts(Pageable pageable) {
        log.debug("Fetching all products with pagination: {}", pageable);

        return productRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    /**
     * Search products by name or description
     */
    @Transactional(readOnly = true)
    public Page<ProductDto.Response> searchProducts(String searchTerm, Pageable pageable) {
        log.debug("Searching products with term: {}", searchTerm);

        return productRepository.searchProducts(searchTerm, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get products by price range
     */
    @Transactional(readOnly = true)
    public Page<ProductDto.Response> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        log.debug("Fetching products with price range: {} - {}", minPrice, maxPrice);

        return productRepository.findByPriceBetween(minPrice, maxPrice, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get products in stock
     */
    @Transactional(readOnly = true)
    public Page<ProductDto.Response> getInStockProducts(Pageable pageable) {
        log.debug("Fetching in-stock products");

        return productRepository.findInStockProducts(pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get products out of stock
     */
    @Transactional(readOnly = true)
    public Page<ProductDto.Response> getOutOfStockProducts(Pageable pageable) {
        log.debug("Fetching out-of-stock products");

        return productRepository.findOutOfStockProducts(pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get low stock products
     */
    @Transactional(readOnly = true)
    public List<ProductDto.Response> getLowStockProducts(Integer threshold) {
        log.debug("Fetching low stock products with threshold: {}", threshold);

        return productRepository.findLowStockProducts(threshold)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Update product information
     */
    public ProductDto.Response updateProduct(Long productId, ProductDto.UpdateRequest request) {
        log.info("Updating product with ID: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getStockQty() != null) {
            product.setStockQty(request.getStockQty());
        }
        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully with ID: {}", updatedProduct.getProductId());

        return mapToResponse(updatedProduct);
    }

    /**
     * Update product stock
     */
    public ProductDto.Response updateStock(Long productId, ProductDto.StockUpdateRequest request) {
        log.info("Updating stock for product ID: {} - {} {}", productId, request.getOperation(), request.getQuantity());

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        if ("increase".equalsIgnoreCase(request.getOperation())) {
            product.increaseStock(request.getQuantity());
        } else if ("decrease".equalsIgnoreCase(request.getOperation())) {
            product.reduceStock(request.getQuantity());
        } else {
            throw new IllegalArgumentException("Invalid operation. Use 'increase' or 'decrease'");
        }

        Product updatedProduct = productRepository.save(product);
        log.info("Stock updated successfully for product ID: {}, new stock: {}", 
                updatedProduct.getProductId(), updatedProduct.getStockQty());

        return mapToResponse(updatedProduct);
    }

    /**
     * Reduce stock for order processing
     */
    public void reduceStock(Long productId, Integer quantity) {
        log.info("Reducing stock for product ID: {} by quantity: {}", productId, quantity);

        int updatedRows = productRepository.reduceStock(productId, quantity);
        if (updatedRows == 0) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
            
            if (!product.hasStock(quantity)) {
                throw new IllegalArgumentException("Insufficient stock. Available: " + product.getStockQty() + ", Requested: " + quantity);
            }
        }
    }

    /**
     * Increase stock (for returns or restocking)
     */
    public void increaseStock(Long productId, Integer quantity) {
        log.info("Increasing stock for product ID: {} by quantity: {}", productId, quantity);

        int updatedRows = productRepository.increaseStock(productId, quantity);
        if (updatedRows == 0) {
            throw new ResourceNotFoundException("Product not found with ID: " + productId);
        }
    }

    /**
     * Delete product
     */
    public void deleteProduct(Long productId) {
        log.info("Deleting product with ID: {}", productId);

        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with ID: " + productId);
        }

        productRepository.deleteById(productId);
        log.info("Product deleted successfully with ID: {}", productId);
    }

    /**
     * Get products created after a specific date
     */
    @Transactional(readOnly = true)
    public Page<ProductDto.Response> getProductsCreatedAfter(LocalDateTime date, Pageable pageable) {
        log.debug("Fetching products created after: {}", date);

        return productRepository.findByCreatedAtAfter(date, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get top selling products
     */
    @Transactional(readOnly = true)
    public Page<ProductDto.Response> getTopSellingProducts(Pageable pageable) {
        log.debug("Fetching top selling products");

        return productRepository.findTopSellingProducts(pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get available products by price range
     */
    @Transactional(readOnly = true)
    public Page<ProductDto.Response> getAvailableProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        log.debug("Fetching available products with price range: {} - {}", minPrice, maxPrice);

        return productRepository.findAvailableProductsByPriceRange(minPrice, maxPrice, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get stock counts
     */
    @Transactional(readOnly = true)
    public ProductDto.Statistics getStockStatistics() {
        long inStockCount = productRepository.countInStockProducts();
        long outOfStockCount = productRepository.countOutOfStockProducts();

        return ProductDto.Statistics.builder()
                .totalQuantity(inStockCount + outOfStockCount)
                .orderCount(inStockCount)
                .build();
    }

    /**
     * Check if product has sufficient stock
     */
    @Transactional(readOnly = true)
    public boolean hasStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
        
        return product.hasStock(quantity);
    }

    /**
     * Map Product entity to Response DTO
     */
    private ProductDto.Response mapToResponse(Product product) {
        return ProductDto.Response.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQty(product.getStockQty())
                .imageUrl(product.getImageUrl())
                .createdAt(product.getCreatedAt())
                .inStock(product.isInStock())
                .build();
    }
}