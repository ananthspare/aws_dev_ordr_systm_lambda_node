import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { CartService } from '../core/services/cart.service';
import { Cart, CartItem } from '../core/models/cart.model';

@Component({
  selector: 'app-cart-debug',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule
  ],
  template: `
    <div class="debug-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>🐛 Cart Debug Information</mat-card-title>
        </mat-card-header>
        
        <mat-card-content>
          <div class="debug-section">
            <h3>Cart API Response</h3>
            <pre>{{ cartJson }}</pre>
          </div>
          
          <div class="debug-section" *ngIf="cart && cart.items.length > 0">
            <h3>Cart Items Analysis</h3>
            <div *ngFor="let item of cart.items; let i = index" class="item-debug">
              <h4>Item {{ i + 1 }}: {{ item.productName }}</h4>
              <div class="debug-grid">
                <div><strong>Product ID:</strong> {{ item.productId }}</div>
                <div><strong>Image URL:</strong> 
                  <span [class.has-image]="hasImageUrl(item)" [class.no-image]="!hasImageUrl(item)">
                    {{ item.imageUrl || 'NULL/EMPTY' }}
                  </span>
                </div>
                <div><strong>Image Status:</strong> 
                  <span [class.status-good]="hasImageUrl(item)" [class.status-bad]="!hasImageUrl(item)">
                    {{ hasImageUrl(item) ? '✅ HAS IMAGE' : '❌ NO IMAGE' }}
                  </span>
                </div>
                <div><strong>Test Image:</strong>
                  <img *ngIf="hasImageUrl(item)" 
                       [src]="item.imageUrl" 
                       (load)="onImageLoad(item.productId)"
                       (error)="onImageError(item.productId)"
                       style="width: 50px; height: 50px; object-fit: cover; margin-left: 10px;">
                  <span *ngIf="!hasImageUrl(item)">No URL to test</span>
                </div>
                <div><strong>Load Status:</strong> 
                  <span [class.status-good]="imageLoadStatus[item.productId] === 'loaded'"
                        [class.status-bad]="imageLoadStatus[item.productId] === 'error'"
                        [class.status-pending]="imageLoadStatus[item.productId] === 'loading'">
                    {{ imageLoadStatus[item.productId] || 'Not tested' }}
                  </span>
                </div>
              </div>
            </div>
          </div>
          
          <div class="debug-section">
            <h3>Troubleshooting Steps</h3>
            <ol>
              <li>Check if cart items have imageUrl field populated</li>
              <li>Verify S3 URLs are accessible</li>
              <li>Check browser network tab for failed image requests</li>
              <li>Ensure database has correct S3 URLs</li>
              <li>Verify backend CartService populates imageUrl</li>
            </ol>
          </div>
          
          <div class="debug-section">
            <h3>Quick Actions</h3>
            <button mat-raised-button color="primary" (click)="refreshCart()">
              <mat-icon>refresh</mat-icon>
              Refresh Cart
            </button>
            <button mat-raised-button color="accent" (click)="testS3Connection()" style="margin-left: 10px;">
              <mat-icon>cloud</mat-icon>
              Test S3 Connection
            </button>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .debug-container {
      padding: 20px;
      max-width: 1000px;
      margin: 0 auto;
    }
    
    .debug-section {
      margin-bottom: 30px;
      padding: 15px;
      border: 1px solid #e0e0e0;
      border-radius: 8px;
      background: #fafafa;
    }
    
    .debug-section h3 {
      margin-top: 0;
      color: #333;
    }
    
    pre {
      background: #f5f5f5;
      padding: 15px;
      border-radius: 4px;
      overflow-x: auto;
      font-size: 12px;
      max-height: 300px;
    }
    
    .item-debug {
      margin-bottom: 20px;
      padding: 15px;
      border: 1px solid #ddd;
      border-radius: 6px;
      background: white;
    }
    
    .item-debug h4 {
      margin-top: 0;
      color: #1976d2;
    }
    
    .debug-grid {
      display: grid;
      grid-template-columns: 150px 1fr;
      gap: 10px;
      align-items: center;
    }
    
    .has-image {
      color: #2e7d32;
      font-weight: 500;
    }
    
    .no-image {
      color: #d32f2f;
      font-weight: 500;
    }
    
    .status-good {
      color: #2e7d32;
      font-weight: bold;
    }
    
    .status-bad {
      color: #d32f2f;
      font-weight: bold;
    }
    
    .status-pending {
      color: #f57c00;
      font-weight: bold;
    }
    
    ol li {
      margin-bottom: 8px;
    }
  `]
})
export class CartDebugComponent implements OnInit {
  private cartService = inject(CartService);
  
  cart: Cart | null = null;
  cartJson: string = '';
  imageLoadStatus: { [key: number]: string } = {};
  
  ngOnInit(): void {
    this.loadCart();
  }
  
  loadCart(): void {
    this.cartService.getCart().subscribe({
      next: (cart) => {
        this.cart = cart;
        this.cartJson = JSON.stringify(cart, null, 2);
        
        // Initialize image load status
        if (cart && cart.items) {
          cart.items.forEach(item => {
            if (this.hasImageUrl(item)) {
              this.imageLoadStatus[item.productId] = 'loading';
            }
          });
        }
      },
      error: (error) => {
        this.cartJson = `ERROR: ${JSON.stringify(error, null, 2)}`;
      }
    });
  }
  
  refreshCart(): void {
    this.imageLoadStatus = {};
    this.loadCart();
  }
  
  hasImageUrl(item: CartItem): boolean {
    return !!(item.imageUrl && item.imageUrl.trim() !== '');
  }
  
  onImageLoad(productId: number): void {
    this.imageLoadStatus[productId] = 'loaded';
  }
  
  onImageError(productId: number): void {
    this.imageLoadStatus[productId] = 'error';
  }
  
  testS3Connection(): void {
    const testUrl = 'https://ecommerce-product-images-test.s3.us-east-1.amazonaws.com/products/iPhone%2015%20Pro.jpg';
    
    fetch(testUrl, { method: 'HEAD' })
      .then(response => {
        if (response.ok) {
          alert('✅ S3 connection successful! Test image is accessible.');
        } else {
          alert(`❌ S3 connection failed! Status: ${response.status} ${response.statusText}`);
        }
      })
      .catch(error => {
        alert(`❌ S3 connection error: ${error.message}`);
      });
  }
}