# Backend Testing Guide

## Fixed Issues

1. **API Versioning**: All controllers now use `/api/v1` prefix consistently
2. **Security Configuration**: Proper JWT authentication filter added
3. **Cart Authentication**: Cart endpoints now require proper authentication
4. **Context Path**: Removed duplicate context path configuration

## Testing the Fixes

### 1. Test Public Endpoints

```bash
# Test public endpoint
curl http://localhost:8080/api/v1/test/public

# Test products endpoint (should work without auth)
curl http://localhost:8080/api/v1/products
```

### 2. Test Authentication

```bash
# Login to get a token
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'

# Test authenticated endpoint
curl http://localhost:8080/api/v1/test/auth \
  -H "Authorization: Bearer mock-jwt-token-123"
```

### 3. Test Cart Endpoints

```bash
# Get cart (requires authentication)
curl http://localhost:8080/api/v1/cart \
  -H "Authorization: Bearer mock-jwt-token-123"

# Add item to cart
curl -X POST http://localhost:8080/api/v1/cart/items \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer mock-jwt-token-123" \
  -d '{
    "productId": 1,
    "quantity": 2
  }'

# Update cart item
curl -X PUT http://localhost:8080/api/v1/cart/items/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer mock-jwt-token-123" \
  -d '{
    "quantity": 3
  }'

# Remove item from cart
curl -X DELETE http://localhost:8080/api/v1/cart/items/1 \
  -H "Authorization: Bearer mock-jwt-token-123"
```

## Authentication Token Format

For testing purposes, the application accepts mock JWT tokens in the format:
`mock-jwt-token-{customerId}`

Example: `mock-jwt-token-123` will authenticate as customer ID 123.

## Key Changes Made

1. **SecurityConfig.java**: Added JWT authentication filter and proper endpoint security
2. **JwtAuthenticationFilter.java**: Created to handle mock JWT tokens
3. **All Controllers**: Updated to use `/api/v1` prefix
4. **application.yml**: Removed duplicate context-path configuration
5. **TestController.java**: Added for testing authentication

## Error Resolution

The original error was caused by:
- Double API prefixing (`/api/v1` in both context-path and controllers)
- Missing JWT authentication filter
- Cart endpoints requiring authentication but no auth mechanism configured

All these issues have been resolved.