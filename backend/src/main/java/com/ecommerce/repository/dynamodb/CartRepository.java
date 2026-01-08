package com.ecommerce.repository.dynamodb;

import com.ecommerce.entity.dynamodb.CartItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CartRepository {

    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;
    private static final String TABLE_NAME = "Cart";

    private DynamoDbTable<CartItem> getTable() {
        return dynamoDbEnhancedClient.table(TABLE_NAME, TableSchema.fromBean(CartItem.class));
    }

    /**
     * Get all cart items for a customer
     */
    public List<CartItem> findByCustomerId(String customerId) {
        try {
            DynamoDbTable<CartItem> table = getTable();
            
            QueryConditional queryConditional = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(customerId).build()
            );
            
            QueryEnhancedRequest queryRequest = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .build();
            
            return table.query(queryRequest)
                .items()
                .stream()
                .collect(Collectors.toList());
                
        } catch (DynamoDbException e) {
            log.error("Error retrieving cart items for customer {}: {}", customerId, e.getMessage());
            throw new RuntimeException("Failed to retrieve cart items", e);
        }
    }

    /**
     * Get a specific cart item by customerId and productId
     */
    public Optional<CartItem> findByCustomerIdAndProductId(String customerId, Long productId) {
        try {
            DynamoDbTable<CartItem> table = getTable();
            
            String itemKey = "PRODUCT#" + productId;
            Key key = Key.builder()
                .partitionValue(customerId)
                .sortValue(itemKey)
                .build();
            
            CartItem item = table.getItem(key);
            return Optional.ofNullable(item);
            
        } catch (DynamoDbException e) {
            log.error("Error retrieving cart item for customer {} and product {}: {}", 
                customerId, productId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Save or update a cart item
     */
    public CartItem save(CartItem cartItem) {
        try {
            DynamoDbTable<CartItem> table = getTable();
            
            // Ensure itemKey is set correctly
            if (cartItem.getItemKey() == null && cartItem.getProductId() != null) {
                cartItem.setItemKey("PRODUCT#" + cartItem.getProductId());
            }
            
            // Set timestamps and TTL
            cartItem.setTimestamps();
            cartItem.calculateTotalPrice();
            
            table.putItem(cartItem);
            log.info("Saved cart item for customer {} and product {}", 
                cartItem.getCustomerId(), cartItem.getProductId());
            
            return cartItem;
            
        } catch (DynamoDbException e) {
            log.error("Error saving cart item for customer {} and product {}: {}", 
                cartItem.getCustomerId(), cartItem.getProductId(), e.getMessage());
            throw new RuntimeException("Failed to save cart item", e);
        }
    }

    /**
     * Delete a specific cart item
     */
    public void deleteByCustomerIdAndProductId(String customerId, Long productId) {
        try {
            DynamoDbTable<CartItem> table = getTable();
            
            String itemKey = "PRODUCT#" + productId;
            Key key = Key.builder()
                .partitionValue(customerId)
                .sortValue(itemKey)
                .build();
            
            table.deleteItem(key);
            log.info("Deleted cart item for customer {} and product {}", customerId, productId);
            
        } catch (DynamoDbException e) {
            log.error("Error deleting cart item for customer {} and product {}: {}", 
                customerId, productId, e.getMessage());
            throw new RuntimeException("Failed to delete cart item", e);
        }
    }

    /**
     * Clear all cart items for a customer
     */
    public void deleteByCustomerId(String customerId) {
        try {
            List<CartItem> items = findByCustomerId(customerId);
            DynamoDbTable<CartItem> table = getTable();
            
            for (CartItem item : items) {
                Key key = Key.builder()
                    .partitionValue(item.getCustomerId())
                    .sortValue(item.getItemKey())
                    .build();
                table.deleteItem(key);
            }
            
            log.info("Cleared all cart items for customer {}", customerId);
            
        } catch (DynamoDbException e) {
            log.error("Error clearing cart for customer {}: {}", customerId, e.getMessage());
            throw new RuntimeException("Failed to clear cart", e);
        }
    }

    /**
     * Check if cart exists for customer
     */
    public boolean existsByCustomerId(String customerId) {
        try {
            List<CartItem> items = findByCustomerId(customerId);
            return !items.isEmpty();
        } catch (Exception e) {
            log.error("Error checking cart existence for customer {}: {}", customerId, e.getMessage());
            return false;
        }
    }

    /**
     * Get cart item count for customer
     */
    public int getCartItemCount(String customerId) {
        try {
            List<CartItem> items = findByCustomerId(customerId);
            return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
        } catch (Exception e) {
            log.error("Error getting cart item count for customer {}: {}", customerId, e.getMessage());
            return 0;
        }
    }

    /**
     * Get cart items by customer ID (convenience method for Long customerId)
     */
    public List<CartItem> findByCustomerId(Long customerId) {
        return findByCustomerId(String.valueOf(customerId));
    }

    /**
     * Get specific cart item (convenience method for Long customerId)
     */
    public Optional<CartItem> findByCustomerIdAndProductId(Long customerId, Long productId) {
        return findByCustomerIdAndProductId(String.valueOf(customerId), productId);
    }

    /**
     * Delete cart item (convenience method for Long customerId)
     */
    public void deleteByCustomerIdAndProductId(Long customerId, Long productId) {
        deleteByCustomerIdAndProductId(String.valueOf(customerId), productId);
    }

    /**
     * Clear cart (convenience method for Long customerId)
     */
    public void deleteByCustomerId(Long customerId) {
        deleteByCustomerId(String.valueOf(customerId));
    }

    /**
     * Check cart existence (convenience method for Long customerId)
     */
    public boolean existsByCustomerId(Long customerId) {
        return existsByCustomerId(String.valueOf(customerId));
    }

    /**
     * Get cart item count (convenience method for Long customerId)
     */
    public int getCartItemCount(Long customerId) {
        return getCartItemCount(String.valueOf(customerId));
    }
}