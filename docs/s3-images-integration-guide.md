# S3 Product Images Integration Guide

This guide walks you through the complete process of integrating S3 product images with your e-commerce application.

## Overview

The application now supports storing product images in Amazon S3 and displaying them in the frontend. The integration includes:

- S3 bucket setup with public access for product images
- Backend support for storing image URLs in the database
- Frontend components that display S3 images with fallback placeholders
- Cart functionality that includes product images

## Prerequisites

- AWS CLI installed and configured
- AWS account with S3 access permissions
- Running e-commerce application (backend + frontend)

## Step 1: Set Up S3 Bucket

### Option A: Automated Setup (Recommended)

Run the automated setup script:

```bash
cd scripts
chmod +x setup-s3-images.sh
./setup-s3-images.sh
```

This will:
- Create a uniquely named S3 bucket
- Configure public access permissions
- Set up folder structure
- Generate configuration files

### Option B: Manual Setup

Follow the detailed instructions in `infrastructure/s3-product-images-setup.md`.

## Step 2: Upload Sample Images

### Prepare Your Images

Create a local folder structure:

```
sample-images/
├── products/
│   ├── electronics/
│   │   ├── gaming-laptop-pro.jpg
│   │   ├── smartphone-x1.jpg
│   │   ├── headphones-nc.jpg
│   │   ├── smart-tv-55.jpg
│   │   └── earbuds-pro.jpg
│   ├── clothing/
│   │   ├── cotton-tshirt.jpg
│   │   ├── denim-jeans.jpg
│   │   ├── puffer-jacket.jpg
│   │   ├── running-sneakers.jpg
│   │   └── casual-hoodie.jpg
│   ├── books/
│   │   ├── javascript-guide.jpg
│   │   ├── clean-code.jpg
│   │   ├── mystery-novel.jpg
│   │   ├── healthy-cookbook.jpg
│   │   └── data-science.jpg
│   ├── home/
│   │   ├── office-chair.jpg
│   │   ├── desk-lamp.jpg
│   │   ├── plant-collection.jpg
│   │   ├── coffee-maker.jpg
│   │   └── security-camera.jpg
│   └── sports/
│       ├── yoga-mat.jpg
│       ├── dumbbells-set.jpg
│       ├── fitness-tracker.jpg
│       ├── basketball.jpg
│       └── resistance-bands.jpg
└── thumbnails/
    └── (same structure as products for smaller versions)
```

### Upload Images

Use the upload script:

```bash
cd scripts
chmod +x upload-sample-images.sh
./upload-sample-images.sh your-bucket-name ./sample-images
```

Or upload manually using AWS CLI:

```bash
aws s3 sync ./sample-images/ s3://your-bucket-name/ --acl public-read
```

## Step 3: Update Database with S3 URLs

### Update Sample Data

1. Edit `database/sample-data-with-s3-images.sql`
2. Replace `your-bucket-name` with your actual S3 bucket name
3. Run the SQL script to populate products with S3 image URLs:

```sql
-- Example: Update the bucket name in the SQL file
UPDATE products SET image_url = REPLACE(image_url, 'your-bucket-name', 'ecommerce-product-images-20240104');
```

### For Existing Data

If you have existing products without image URLs, update them:

```sql
-- Update specific products with S3 URLs
UPDATE products 
SET image_url = 'https://your-bucket-name.s3.us-east-1.amazonaws.com/products/electronics/gaming-laptop-pro.jpg'
WHERE name = 'Gaming Laptop Pro';

UPDATE products 
SET image_url = 'https://your-bucket-name.s3.us-east-1.amazonaws.com/products/electronics/smartphone-x1.jpg'
WHERE name = 'Wireless Smartphone X1';

-- Add more updates as needed...
```

## Step 4: Test the Integration

### Backend Testing

1. Start your Spring Boot application
2. Test the product API endpoints:

```bash
# Get all products (should include imageUrl field)
curl http://localhost:8080/api/products

# Get specific product
curl http://localhost:8080/api/products/1
```

3. Verify that the response includes `imageUrl` field with S3 URLs

### Frontend Testing

1. Start your Angular application
2. Navigate to the products page
3. Verify that product images load from S3
4. Test cart functionality to ensure cart items show product images

### Image URL Testing

Test that your S3 images are publicly accessible:

```bash
# Test image URL directly
curl -I https://your-bucket-name.s3.us-east-1.amazonaws.com/products/electronics/gaming-laptop-pro.jpg
```

Should return `200 OK` status.

## Step 5: Application Configuration

### Backend Configuration

The application automatically uses the `imageUrl` field from the Product entity. No additional configuration needed.

### Frontend Configuration

The frontend components now:
- Display S3 images when `imageUrl` is available
- Fall back to placeholder images when `imageUrl` is null/empty
- Handle image loading errors gracefully

## Troubleshooting

### Common Issues

1. **403 Forbidden Error**
   - Check bucket policy is applied correctly
   - Verify public access block is disabled
   - Ensure objects have public-read ACL

2. **Images Not Loading**
   - Verify image URLs in database are correct
   - Check browser network tab for failed requests
   - Test image URLs directly in browser

3. **CORS Issues**
   - Add CORS configuration to S3 bucket
   - Allow GET requests from your domain

### Debug Steps

1. Check browser console for errors
2. Verify database contains correct S3 URLs
3. Test S3 URLs directly in browser
4. Check AWS CloudTrail for S3 access logs

## Image Management Best Practices

### Naming Convention

Use consistent naming for easy management:
- `{category}-{product-name}-{variant}.{extension}`
- Example: `electronics-laptop-gaming-1.jpg`

### Image Optimization

- Use appropriate image sizes (300x200 for product list, 400x400 for details)
- Compress images to reduce load times
- Consider using WebP format for better compression

### Security Considerations

- Use CloudFront for better performance and caching
- Enable versioning for backup
- Set up lifecycle policies for cost optimization
- Monitor access logs

## Next Steps

1. **Image Upload Feature**: Add admin functionality to upload new product images
2. **Image Resizing**: Implement automatic thumbnail generation
3. **CDN Integration**: Set up CloudFront for better performance
4. **Image Optimization**: Add image compression and format conversion

## File Changes Summary

### Backend Changes
- `CartItem.java`: Added `imageUrl` field
- `CartItemDto.java`: Added `imageUrl` field  
- `CartService.java`: Updated to populate `imageUrl` from Product entity
- Sample data SQL includes S3 image URLs

### Frontend Changes
- `cart.model.ts`: Added `imageUrl` field to CartItem interface
- `product-list.component.ts`: Enhanced image handling with S3 URLs
- `product-detail.component.ts`: Enhanced image handling with S3 URLs
- `cart.component.ts`: Updated to use actual product images from S3

### New Files
- `database/sample-data-with-s3-images.sql`: Sample data with S3 URLs
- `docs/s3-images-integration-guide.md`: This comprehensive guide
- `infrastructure/s3-product-images-setup.md`: Detailed S3 setup instructions
- `scripts/setup-s3-images.sh`: Automated S3 setup script
- `scripts/upload-sample-images.sh`: Image upload script

The integration is now complete and your e-commerce application supports S3-hosted product images with proper fallback handling!