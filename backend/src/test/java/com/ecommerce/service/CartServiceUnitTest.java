package com.ecommerce.service;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartDto;
import com.ecommerce.dto.CartItemDto;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.dynamodb.CartItem;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.dynamodb.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceUnitTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    private Product testProduct;
    private CartItem testCartItem;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setProductId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setStockQty(10);

        testCartItem = CartItem.builder()
                .customerId("123")
                .productId(1L)
                .itemKey("PRODUCT#1")
                .productName("Test Product")
                .quantity(2)
                .unitPrice(new BigDecimal("99.99"))
                .totalPrice(new BigDecimal("199.98"))
                .addedAt(Instant.now().toString())
                .updatedAt(Instant.now().toString())
                .build();
    }

    @Test
    void testGetCart() {
        // Given
        Long customerId = 123L;
        when(cartRepository.findByCustomerId(String.valueOf(customerId)))
                .thenReturn(Arrays.asList(testCartItem));

        // When
        CartDto result = cartService.getCart(customerId);

        // Then
        assertNotNull(result);
        assertEquals(customerId, result.getCustomerId());
        assertEquals(1, result.getItems().size());
        assertEquals(2, result.getTotalItems());
        assertEquals(new BigDecimal("199.98"), result.getTotalAmount());

        verify(cartRepository).findByCustomerId(String.valueOf(customerId));
    }

    @Test
    void testAddToCart_NewItem() {
        // Given
        Long customerId = 123L;
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(1L);
        request.setQuantity(2);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartRepository.findByCustomerIdAndProductId(String.valueOf(customerId), 1L))
                .thenReturn(Optional.empty());
        when(cartRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        // When
        CartItemDto result = cartService.addToCart(customerId, request);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getProductId());
        assertEquals(2, result.getQuantity());
        assertEquals(new BigDecimal("99.99"), result.getUnitPrice());

        verify(productRepository).findById(1L);
        verify(cartRepository).findByCustomerIdAndProductId(String.valueOf(customerId), 1L);
        verify(cartRepository).save(any(CartItem.class));
    }

    @Test
    void testAddToCart_ExistingItem() {
        // Given
        Long customerId = 123L;
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(1L);
        request.setQuantity(1);

        CartItem existingItem = CartItem.builder()
                .customerId("123")
                .productId(1L)
                .itemKey("PRODUCT#1")
                .quantity(2)
                .unitPrice(new BigDecimal("99.99"))
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartRepository.findByCustomerIdAndProductId(String.valueOf(customerId), 1L))
                .thenReturn(Optional.of(existingItem));
        when(cartRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        // When
        CartItemDto result = cartService.addToCart(customerId, request);

        // Then
        assertNotNull(result);
        verify(cartRepository).save(argThat(item -> item.getQuantity() == 3)); // 2 + 1
    }

    @Test
    void testAddToCart_InsufficientStock() {
        // Given
        Long customerId = 123L;
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(1L);
        request.setQuantity(15); // More than available stock (10)

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cartService.addToCart(customerId, request)
        );

        assertTrue(exception.getMessage().contains("Insufficient stock"));
        verify(productRepository).findById(1L);
        verify(cartRepository, never()).save(any());
    }

    @Test
    void testRemoveFromCart() {
        // Given
        Long customerId = 123L;
        Long productId = 1L;

        when(cartRepository.findByCustomerIdAndProductId(String.valueOf(customerId), productId))
                .thenReturn(Optional.of(testCartItem));

        // When
        cartService.removeFromCart(customerId, productId);

        // Then
        verify(cartRepository).findByCustomerIdAndProductId(String.valueOf(customerId), productId);
        verify(cartRepository).deleteByCustomerIdAndProductId(String.valueOf(customerId), productId);
    }

    @Test
    void testClearCart() {
        // Given
        Long customerId = 123L;

        // When
        cartService.clearCart(customerId);

        // Then
        verify(cartRepository).deleteByCustomerId(String.valueOf(customerId));
    }

    @Test
    void testGetCartItemCount() {
        // Given
        Long customerId = 123L;
        when(cartRepository.getCartItemCount(String.valueOf(customerId))).thenReturn(5);

        // When
        int result = cartService.getCartItemCount(customerId);

        // Then
        assertEquals(5, result);
        verify(cartRepository).getCartItemCount(String.valueOf(customerId));
    }
}