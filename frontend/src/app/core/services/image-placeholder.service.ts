import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ImagePlaceholderService {
  
  private readonly placeholders = {
    product: '/assets/images/placeholder-product.svg',
    cart: '/assets/images/placeholder-cart.svg',
    profile: '/assets/images/placeholder-profile.svg'
  };

  /**
   * Get placeholder image for products
   */
  getProductPlaceholder(size: 'small' | 'medium' | 'large' = 'medium'): string {
    return this.placeholders.product;
  }

  /**
   * Get placeholder image for cart items
   */
  getCartPlaceholder(): string {
    return this.placeholders.cart;
  }

  /**
   * Get placeholder image for user profiles
   */
  getProfilePlaceholder(): string {
    return this.placeholders.profile;
  }

  /**
   * Generate a data URL placeholder with custom text
   */
  generateTextPlaceholder(
    width: number, 
    height: number, 
    text: string, 
    backgroundColor: string = '#f5f5f5',
    textColor: string = '#9e9e9e'
  ): string {
    const canvas = document.createElement('canvas');
    canvas.width = width;
    canvas.height = height;
    
    const ctx = canvas.getContext('2d');
    if (!ctx) return this.placeholders.product;
    
    // Fill background
    ctx.fillStyle = backgroundColor;
    ctx.fillRect(0, 0, width, height);
    
    // Add text
    ctx.fillStyle = textColor;
    ctx.font = `${Math.min(width, height) / 10}px Arial`;
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    
    // Wrap text if too long
    const maxWidth = width * 0.8;
    const words = text.split(' ');
    let line = '';
    const lines: string[] = [];
    
    for (const word of words) {
      const testLine = line + word + ' ';
      const metrics = ctx.measureText(testLine);
      
      if (metrics.width > maxWidth && line !== '') {
        lines.push(line.trim());
        line = word + ' ';
      } else {
        line = testLine;
      }
    }
    lines.push(line.trim());
    
    // Draw lines
    const lineHeight = Math.min(width, height) / 8;
    const startY = height / 2 - (lines.length - 1) * lineHeight / 2;
    
    lines.forEach((line, index) => {
      ctx.fillText(line, width / 2, startY + index * lineHeight);
    });
    
    return canvas.toDataURL();
  }

  /**
   * Create a simple colored placeholder
   */
  createColorPlaceholder(
    width: number, 
    height: number, 
    color: string = '#e3f2fd'
  ): string {
    const canvas = document.createElement('canvas');
    canvas.width = width;
    canvas.height = height;
    
    const ctx = canvas.getContext('2d');
    if (!ctx) return this.placeholders.product;
    
    ctx.fillStyle = color;
    ctx.fillRect(0, 0, width, height);
    
    // Add a simple icon
    ctx.fillStyle = '#1976d2';
    ctx.font = `${Math.min(width, height) / 3}px Arial`;
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText('📷', width / 2, height / 2);
    
    return canvas.toDataURL();
  }

  /**
   * Get appropriate placeholder based on context
   */
  getPlaceholder(
    context: 'product-list' | 'product-detail' | 'cart' | 'order',
    productName?: string
  ): string {
    switch (context) {
      case 'product-list':
        return productName 
          ? this.generateTextPlaceholder(300, 200, productName)
          : this.createColorPlaceholder(300, 200);
      
      case 'product-detail':
        return productName 
          ? this.generateTextPlaceholder(400, 400, productName)
          : this.createColorPlaceholder(400, 400);
      
      case 'cart':
      case 'order':
        return productName 
          ? this.generateTextPlaceholder(80, 80, productName)
          : this.createColorPlaceholder(80, 80);
      
      default:
        return this.placeholders.product;
    }
  }
}