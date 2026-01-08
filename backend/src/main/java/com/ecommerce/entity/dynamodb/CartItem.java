package com.ecommerce.entity.dynamodb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class CartItem {
    
    private String customerId;    // String partition key
    private String itemKey;       // String sort key in format "PRODUCT#{productId}"
    private Long productId;       // Actual product ID as number
    private String productName;
    private String imageUrl;      // Product image URL
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private String addedAt;
    private String updatedAt;
    private Long ttl; // TTL for cart expiration (30 days from last update)

    @DynamoDbPartitionKey
    public String getCustomerId() {
        return customerId;
    }

    @DynamoDbSortKey
    public String getItemKey() {
        return itemKey;
    }

    // Helper method to generate itemKey from productId
    public void setProductId(Long productId) {
        this.productId = productId;
        this.itemKey = "PRODUCT#" + productId;
    }

    // Helper method to extract productId from itemKey
    public Long getProductIdFromItemKey() {
        if (itemKey != null && itemKey.startsWith("PRODUCT#")) {
            try {
                return Long.parseLong(itemKey.substring(8)); // Remove "PRODUCT#" prefix
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    // Helper method to calculate total price
    public void calculateTotalPrice() {
        if (quantity != null && unitPrice != null) {
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    // Helper method to set TTL (30 days from now)
    public void setTtlFromNow() {
        this.ttl = Instant.now().plusSeconds(30 * 24 * 60 * 60).getEpochSecond(); // 30 days
    }

    // Helper method to set timestamps
    public void setTimestamps() {
        String now = Instant.now().toString();
        if (this.addedAt == null) {
            this.addedAt = now;
        }
        this.updatedAt = now;
        setTtlFromNow();
    }

    // Helper method to create CartItem from customerId and productId
    public static CartItem createNew(String customerId, Long productId) {
        CartItem item = new CartItem();
        item.setCustomerId(customerId);
        item.setProductId(productId); // This will also set the itemKey
        return item;
    }
}