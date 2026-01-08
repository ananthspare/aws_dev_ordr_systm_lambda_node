# Cart Images Troubleshooting Guide

## Problem: Shopping Cart Images Not Displaying

This guide helps you debug why product images are not showing in the shopping cart.

## Quick Diagnosis

### Step 1: Check Cart API Response

Open browser dev tools → Network tab → Load cart page → Check the cart API response:

```bash
# Test cart API manually
curl http://localhost:8080/api/cart
```

**Expected Response:**
```json
{
  "customerId": 1,
  "items": [
    {
      "productId": 1,
      "productName": "iPhone 15 Pro",
      "imageUrl": "https://ecommerce-product-images-test.s3.us-east-1.amazonaws.com/products/iPhone%2015%20Pro.jpg",
      "quantity": 1,
      "unitPrice": 999.99
    }
  ]
}
```

### Step 2: Verify Database Has Image URLs

```sql
-- Check if products have image URLs
SELECT product_id, name, image_url 
FROM products 
WHERE image_url IS NOT NULL 
LIMIT 5;
```

### Step 3: Test S3 Image URLs

```bash
# Test a sample S3 URL
curl -I https://ecommerce-product-images-test.s3.us-east-1.amazonaws.com/products/iPhone%2015%20Pro.jpg
```

Should return `200 OK`.

## Common Issues & Solutions

### Issue 1: Cart API Returns imageUrl as null

**Symptoms:**
- Cart API response shows `"imageUrl": null`
- Products API shows correct imageUrl

**Cause:** Backend CartService not populating imageUrl from Product entity

**Solution:**
1. Check if CartItem entity has imageUrl field
2. Verify CartService.addToCart() sets imageUrl
3. Ensure CartService.convertToDto() includes imageUrl

**Fix Applied:**
```java
// In CartService.addToCart()
cartItem = CartItem.builder()
    .productName(product.getName())
    .imageUrl(product.getImageUrl())  // ← This was missing
    .build();
```

### Issue 2: Database Products Missing Image URLs

**Symptoms:**
- Products API returns `"imageUrl": null`
- Database query shows NULL image_url

**Solution:**
```bash
# Run the database update script
mysql -u username -p database_name < database/update-specific-products.sql
```

### Issue 3: S3 Images Not Accessible

**Symptoms:**
- Cart API returns correct imageUrl
- Images fail to load (404/403 errors)

**Solution:**
1. Check S3 bucket permissions
2. Verify bucket policy allows public read
3. Test URLs directly in browser

```bash
# Test S3 connectivity
./scripts/test-s3-integration.sh ecommerce-product-images-test
```

### Issue 4: Frontend Not Using imageUrl

**Symptoms:**
- Cart API returns correct imageUrl
- Frontend still shows placeholder images

**Solution:**
Check cart component's `getProductImage()` method:

```typescript
getProductImage(item: CartItem): string {
  // This should check item.imageUrl first
  if (item.imageUrl && item.imageUrl.trim() !== '') {
    return item.imageUrl;
  }
  return this.imagePlaceholderService.getPlaceholder('cart', item.productName);
}
```

### Issue 5: CORS Issues

**Symptoms:**
- Images fail to load with CORS errors
- Console shows "blocked by CORS policy"

**Solution:**
Add CORS configuration to S3 bucket:

```json
{
  "CORSRules": [
    {
      "AllowedHeaders": ["*"],
      "AllowedMethods": ["GET", "HEAD"],
      "AllowedOrigins": ["*"],
      "ExposeHeaders": [],
      "MaxAgeSeconds": 3000
    }
  ]
}
```

## Debugging Tools

### 1. Cart Debug Component

Add to your app for detailed debugging:

```typescript
// In your component template
<app-cart-debug></app-cart-debug>
```

### 2. Test Scripts

```bash
# Test cart images
chmod +x scripts/test-cart-images.sh
./scripts/test-cart-images.sh http://localhost:8080

# Test S3 integration
chmod +x scripts/test-s3-integration.sh
./scripts/test-s3-integration.sh ecommerce-product-images-test
```

### 3. Browser Dev Tools

1. **Network Tab:** Check for failed image requests
2. **Console Tab:** Look for JavaScript errors
3. **Application Tab:** Check if service worker is caching old responses

## Step-by-Step Fix Process

### 1. Backend Fixes

```bash
# 1. Update database with S3 URLs
mysql -u username -p database_name < database/update-specific-products.sql

# 2. Restart Spring Boot application
./mvnw spring-boot:run

# 3. Test cart API
curl http://localhost:8080/api/cart
```

### 2. Frontend Fixes

```bash
# 1. Clear browser cache
# 2. Restart Angular dev server
ng serve

# 3. Test in browser
# Open http://localhost:4200/cart
```

### 3. S3 Fixes

```bash
# 1. Verify bucket permissions
aws s3api get-bucket-policy --bucket ecommerce-product-images-test

# 2. Test image accessibility
curl -I https://ecommerce-product-images-test.s3.us-east-1.amazonaws.com/products/iPhone%2015%20Pro.jpg

# 3. Upload missing images
aws s3 cp local-image.jpg s3://ecommerce-product-images-test/products/iPhone%2015%20Pro.jpg --acl public-read
```

## Verification Checklist

- [ ] Database products have non-null image_url
- [ ] Cart API response includes imageUrl field
- [ ] S3 URLs are publicly accessible (return 200 OK)
- [ ] Frontend cart component uses item.imageUrl
- [ ] Browser dev tools show no failed image requests
- [ ] Images display correctly in cart

## Advanced Debugging

### Enable Debug Logging

Add to `application.yml`:

```yaml
logging:
  level:
    com.ecommerce.service.CartService: DEBUG
    com.ecommerce.repository.dynamodb: DEBUG
```

### Check DynamoDB Items

```bash
# List cart items in DynamoDB
aws dynamodb scan --table-name Cart --limit 5

# Check specific cart item
aws dynamodb get-item --table-name Cart --key '{"customerId":{"S":"1"},"itemKey":{"S":"PRODUCT#1"}}'
```

### Monitor S3 Access Logs

Enable S3 access logging to see if requests are reaching your bucket.

## Still Not Working?

1. **Clear everything and start fresh:**
   ```bash
   # Clear cart
   curl -X DELETE http://localhost:8080/api/cart
   
   # Add item again
   curl -X POST http://localhost:8080/api/cart -H "Content-Type: application/json" -d '{"productId":1,"quantity":1}'
   ```

2. **Check the complete data flow:**
   - Product in database has imageUrl ✓
   - CartService copies imageUrl to CartItem ✓
   - CartItem saved to DynamoDB with imageUrl ✓
   - Cart API returns imageUrl in response ✓
   - Frontend uses imageUrl from API response ✓
   - S3 URL is accessible ✓

3. **Contact support with:**
   - Cart API response JSON
   - Browser network tab screenshot
   - Database query results
   - S3 URL test results

The most common issue is that existing cart items were created before the imageUrl field was added to the backend. Clear your cart and add items again to get the updated cart items with image URLs.