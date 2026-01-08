package com.ecommerce.controller;

import com.ecommerce.dto.ProductDto;
import com.ecommerce.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Management", description = "APIs for managing products")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Create a new product", description = "Creates a new product in the catalog")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Product created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    public ResponseEntity<ProductDto.Response> createProduct(
            @Valid @RequestBody ProductDto.CreateRequest request) {
        String traceId = MDC.get("traceId");
        log.info("[{}] Creating new product: {}", traceId, request.getName());
        log.debug("[{}] Product details - Price: ${}, Stock: {}", 
                traceId, request.getPrice(), request.getStockQty());
        
        try {
            long startTime = System.currentTimeMillis();
            ProductDto.Response response = productService.createProduct(request);
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("[{}] Product created successfully - ID: {}, Name: {}, Duration: {}ms", 
                    traceId, response.getProductId(), response.getName(), duration);
            
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("[{}] Failed to create product: {} - Error: {}", 
                    traceId, request.getName(), e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Get product by ID", description = "Retrieves a product by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/{productId}")
    public ResponseEntity<ProductDto.Response> getProductById(
            @Parameter(description = "Product ID") @PathVariable Long productId) {
        log.info("Fetching product with ID: {}", productId);
        ProductDto.Response response = productService.getProductById(productId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all products", description = "Retrieves all products with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<Page<ProductDto.Response>> getAllProducts(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Fetching all products - page: {}, size: {}, sortBy: {}, sortDir: {}", page, size, sortBy, sortDir);
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductDto.Response> response = productService.getAllProducts(pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Search products", description = "Search products by name or description")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    @GetMapping("/search")
    public ResponseEntity<Page<ProductDto.Response>> searchProducts(
            @Parameter(description = "Search term") @RequestParam String searchTerm,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        
        log.info("Searching products with term: {}", searchTerm);
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductDto.Response> response = productService.searchProducts(searchTerm, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get products by price range", description = "Retrieves products within a price range")
    @GetMapping("/price-range")
    public ResponseEntity<Page<ProductDto.Response>> getProductsByPriceRange(
            @Parameter(description = "Minimum price") @RequestParam BigDecimal minPrice,
            @Parameter(description = "Maximum price") @RequestParam BigDecimal maxPrice,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "price") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        
        log.info("Fetching products with price range: {} - {}", minPrice, maxPrice);
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductDto.Response> response = productService.getProductsByPriceRange(minPrice, maxPrice, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get in-stock products", description = "Retrieves products that are currently in stock")
    @GetMapping("/in-stock")
    public ResponseEntity<Page<ProductDto.Response>> getInStockProducts(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "stockQty") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Fetching in-stock products");
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductDto.Response> response = productService.getInStockProducts(pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get out-of-stock products", description = "Retrieves products that are out of stock")
    @GetMapping("/out-of-stock")
    public ResponseEntity<Page<ProductDto.Response>> getOutOfStockProducts(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Fetching out-of-stock products");
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<ProductDto.Response> response = productService.getOutOfStockProducts(pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get low stock products", description = "Retrieves products with stock below threshold")
    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductDto.Response>> getLowStockProducts(
            @Parameter(description = "Stock threshold") @RequestParam(defaultValue = "10") Integer threshold) {
        
        log.info("Fetching low stock products with threshold: {}", threshold);
        
        List<ProductDto.Response> response = productService.getLowStockProducts(threshold);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update product", description = "Updates product information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PutMapping("/{productId}")
    public ResponseEntity<ProductDto.Response> updateProduct(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Valid @RequestBody ProductDto.UpdateRequest request) {
        log.info("Updating product with ID: {}", productId);
        ProductDto.Response response = productService.updateProduct(productId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update product stock", description = "Updates product stock quantity")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid operation or insufficient stock"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PutMapping("/{productId}/stock")
    public ResponseEntity<ProductDto.Response> updateStock(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Valid @RequestBody ProductDto.StockUpdateRequest request) {
        log.info("Updating stock for product ID: {}", productId);
        ProductDto.Response response = productService.updateStock(productId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete product", description = "Deletes a product from the catalog")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product ID") @PathVariable Long productId) {
        log.info("Deleting product with ID: {}", productId);
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get products created after date", description = "Retrieves products created after a specific date")
    @GetMapping("/created-after")
    public ResponseEntity<Page<ProductDto.Response>> getProductsCreatedAfter(
            @Parameter(description = "Date in ISO format") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Fetching products created after: {}", date);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ProductDto.Response> response = productService.getProductsCreatedAfter(date, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get top selling products", description = "Retrieves top selling products by quantity")
    @GetMapping("/top-selling")
    public ResponseEntity<Page<ProductDto.Response>> getTopSellingProducts(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        log.info("Fetching top selling products");
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDto.Response> response = productService.getTopSellingProducts(pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get available products by price range", description = "Retrieves available products within a price range")
    @GetMapping("/available/price-range")
    public ResponseEntity<Page<ProductDto.Response>> getAvailableProductsByPriceRange(
            @Parameter(description = "Minimum price") @RequestParam BigDecimal minPrice,
            @Parameter(description = "Maximum price") @RequestParam BigDecimal maxPrice,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Fetching available products with price range: {} - {}", minPrice, maxPrice);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("price").ascending());
        Page<ProductDto.Response> response = productService.getAvailableProductsByPriceRange(minPrice, maxPrice, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get stock statistics", description = "Returns stock statistics")
    @GetMapping("/statistics/stock")
    public ResponseEntity<ProductDto.Statistics> getStockStatistics() {
        log.info("Fetching stock statistics");
        ProductDto.Statistics statistics = productService.getStockStatistics();
        return ResponseEntity.ok(statistics);
    }

    @Operation(summary = "Check product stock", description = "Checks if product has sufficient stock")
    @GetMapping("/{productId}/stock/check")
    public ResponseEntity<Boolean> checkStock(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Parameter(description = "Required quantity") @RequestParam Integer quantity) {
        log.info("Checking stock for product ID: {} with quantity: {}", productId, quantity);
        boolean hasStock = productService.hasStock(productId, quantity);
        return ResponseEntity.ok(hasStock);
    }
}