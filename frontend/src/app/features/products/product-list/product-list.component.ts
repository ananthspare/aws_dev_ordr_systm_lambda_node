import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatChipsModule } from '@angular/material/chips';
import { Observable } from 'rxjs';
import { ProductService } from '../../../core/services/product.service';
import { CartService } from '../../../core/services/cart.service';
import { AuthService } from '../../../core/services/auth.service';
import { ImagePlaceholderService } from '../../../core/services/image-placeholder.service';
import { Product } from '../../../core/models/product.model';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatGridListModule,
    MatChipsModule
  ],
  template: `
    <div class="product-list-container">
      <div class="header">
        <h1>Products</h1>
        <p>Discover our amazing collection</p>
      </div>

      <div *ngIf="isLoading" class="loading-container">
        <mat-spinner></mat-spinner>
        <p>Loading products...</p>
      </div>

      <div *ngIf="!isLoading && products.length === 0" class="empty-state">
        <mat-icon>inventory_2</mat-icon>
        <h2>No products available</h2>
        <p>Check back later for new products!</p>
      </div>

      <mat-grid-list *ngIf="!isLoading && products.length > 0" 
                     [cols]="getColumns()" 
                     rowHeight="400px" 
                     gutterSize="20px" 
                     class="product-grid">
        <mat-grid-tile *ngFor="let product of products">
          <mat-card class="product-card">
            <div class="product-image">
              <img [src]="getProductImage(product)" 
                   [alt]="product.name" 
                   (error)="onImageError($event)"
                   (load)="onImageLoad($event)"
                   [class.loading]="!isImageLoaded(product.productId)">
              <div class="image-placeholder" *ngIf="!isImageLoaded(product.productId)">
                <mat-icon>image</mat-icon>
              </div>
            </div>
            
            <mat-card-content>
              <h3 class="product-name">{{ product.name }}</h3>
              <p class="product-description">{{ product.description }}</p>
              
              <div class="product-info">
                <div class="price">
                  <span class="currency">$</span>
                  <span class="amount">{{ product.price | number:'1.2-2' }}</span>
                </div>
                
                <mat-chip-set>
                  <mat-chip [color]="getStockColor(product.stockQty)" selected>
                    <mat-icon>inventory</mat-icon>
                    {{ getStockText(product.stockQty) }}
                  </mat-chip>
                </mat-chip-set>
              </div>
            </mat-card-content>

            <mat-card-actions>
              <button mat-button color="primary" [routerLink]="['/products', product.productId]">
                <mat-icon>visibility</mat-icon>
                View Details
              </button>
              
              <!-- Show quantity controls if item is in cart, otherwise show Add to Cart -->
              <div class="cart-controls">
                <div *ngIf="getProductQuantityInCart(product.productId) | async as quantity; else addToCartButton" 
                     class="quantity-controls">
                  <button mat-icon-button 
                          color="primary"
                          [disabled]="isUpdatingQuantity[product.productId]"
                          (click)="updateCartQuantity(product.productId, quantity - 1)">
                    <mat-icon>remove</mat-icon>
                  </button>
                  
                  <span class="quantity-display">{{ quantity }}</span>
                  
                  <button mat-icon-button 
                          color="primary"
                          [disabled]="isUpdatingQuantity[product.productId] || quantity >= product.stockQty"
                          (click)="updateCartQuantity(product.productId, quantity + 1)">
                    <mat-icon>add</mat-icon>
                  </button>
                </div>
                
                <ng-template #addToCartButton>
                  <button mat-raised-button 
                          color="accent" 
                          [disabled]="product.stockQty === 0 || isAddingToCart[product.productId]"
                          (click)="addToCart(product)">
                    <mat-spinner diameter="20" *ngIf="isAddingToCart[product.productId]"></mat-spinner>
                    <mat-icon *ngIf="!isAddingToCart[product.productId]">add_shopping_cart</mat-icon>
                    <span *ngIf="!isAddingToCart[product.productId]">Add to Cart</span>
                  </button>
                </ng-template>
              </div>
            </mat-card-actions>
          </mat-card>
        </mat-grid-tile>
      </mat-grid-list>
    </div>
  `,
  styles: [`
    .product-list-container {
      padding: 20px;
      max-width: 1200px;
      margin: 0 auto;
    }

    .header {
      text-align: center;
      margin-bottom: 40px;
    }

    .header h1 {
      font-size: 2.5em;
      margin-bottom: 8px;
      color: #333;
    }

    .header p {
      font-size: 1.1em;
      color: #666;
    }

    .loading-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 300px;
      gap: 20px;
    }

    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 300px;
      color: #666;
    }

    .empty-state mat-icon {
      font-size: 64px;
      width: 64px;
      height: 64px;
      margin-bottom: 16px;
    }

    .product-grid {
      margin-top: 20px;
    }

    .product-card {
      width: 100%;
      height: 100%;
      display: flex;
      flex-direction: column;
      cursor: pointer;
      transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
    }

    .product-card:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 25px rgba(0,0,0,0.15);
    }

    .product-image {
      height: 200px;
      overflow: hidden;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f5f5f5;
    }

    .product-image img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: opacity 0.3s ease;
    }

    .product-image img.loading {
      opacity: 0;
    }

    .image-placeholder {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f0f0f0;
      color: #ccc;
    }

    .image-placeholder mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
    }

    .product-name {
      font-size: 1.2em;
      font-weight: 500;
      margin: 0 0 8px 0;
      color: #333;
    }

    .product-description {
      color: #666;
      font-size: 0.9em;
      margin: 0 0 16px 0;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .product-info {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
    }

    .price {
      display: flex;
      align-items: baseline;
      font-weight: 600;
      color: #2e7d32;
    }

    .currency {
      font-size: 0.9em;
      margin-right: 2px;
    }

    .amount {
      font-size: 1.3em;
    }

    mat-card-actions {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px;
      margin-top: auto;
    }

    .cart-controls {
      display: flex;
      align-items: center;
    }

    .quantity-controls {
      display: flex;
      align-items: center;
      gap: 8px;
      background: rgba(63, 81, 181, 0.1);
      border-radius: 20px;
      padding: 4px 8px;
    }

    .quantity-display {
      font-weight: 600;
      color: #3f51b5;
      min-width: 20px;
      text-align: center;
    }

    @media (max-width: 1200px) {
      .product-list-container {
        padding: 16px;
      }
    }

    @media (max-width: 768px) {
      .header h1 {
        font-size: 2em;
      }
      
      mat-card-actions {
        flex-direction: column;
        gap: 8px;
      }
    }
  `]
})
export class ProductListComponent implements OnInit {
  private productService = inject(ProductService);
  private cartService = inject(CartService);
  private authService = inject(AuthService);
  private snackBar = inject(MatSnackBar);
  private imagePlaceholderService = inject(ImagePlaceholderService);

  products: Product[] = [];
  isLoading = true;
  isAddingToCart: { [key: number]: boolean } = {};
  isUpdatingQuantity: { [key: number]: boolean } = {};
  loadedImages: { [key: number]: boolean } = {};

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.isLoading = true;
    this.productService.getAllProducts().subscribe({
      next: (products) => {
        this.products = products;
        this.isLoading = false;
      },
      error: (error: any) => {
        this.isLoading = false;
        this.snackBar.open('Failed to load products', 'Close', { duration: 3000 });
        console.error('Error loading products:', error);
      }
    });
  }

  addToCart(product: Product): void {
    if (!this.authService.isAuthenticated()) {
      this.snackBar.open('Please login to add items to cart', 'Login', { duration: 3000 })
        .onAction().subscribe(() => {
          // Navigate to login - you might want to implement this
        });
      return;
    }

    this.isAddingToCart[product.productId] = true;
    
    this.cartService.addToCart(product.productId, 1).subscribe({
      next: () => {
        this.isAddingToCart[product.productId] = false;
        this.snackBar.open(`${product.name} added to cart!`, 'Close', { duration: 2000 });
      },
      error: (error: any) => {
        this.isAddingToCart[product.productId] = false;
        const message = error.error?.message || 'Failed to add item to cart';
        this.snackBar.open(message, 'Close', { duration: 3000 });
      }
    });
  }

  updateCartQuantity(productId: number, newQuantity: number): void {
    if (!this.authService.isAuthenticated()) {
      return;
    }

    if (newQuantity <= 0) {
      // Remove item from cart
      this.cartService.removeFromCart(productId).subscribe({
        next: () => {
          this.snackBar.open('Item removed from cart', 'Close', { duration: 2000 });
        },
        error: (error: any) => {
          const message = error.error?.message || 'Failed to remove item from cart';
          this.snackBar.open(message, 'Close', { duration: 3000 });
        }
      });
      return;
    }

    this.isUpdatingQuantity[productId] = true;
    
    this.cartService.updateCartItem(productId, newQuantity).subscribe({
      next: () => {
        this.isUpdatingQuantity[productId] = false;
      },
      error: (error: any) => {
        this.isUpdatingQuantity[productId] = false;
        const message = error.error?.message || 'Failed to update quantity';
        this.snackBar.open(message, 'Close', { duration: 3000 });
      }
    });
  }

  getProductQuantityInCart(productId: number): Observable<number> {
    return this.cartService.getProductQuantityInCart(productId);
  }

  getColumns(): number {
    const width = window.innerWidth;
    if (width < 600) return 1;
    if (width < 960) return 2;
    if (width < 1280) return 3;
    return 4;
  }

  getProductImage(product: Product): string {
    // Use the imageUrl from the product if available, otherwise use placeholder
    if (product.imageUrl && product.imageUrl.trim() !== '') {
      return product.imageUrl;
    }
    // Use local placeholder service instead of external API
    return this.imagePlaceholderService.getPlaceholder('product-list', product.name);
  }

  onImageError(event: any): void {
    // Use local placeholder instead of external API
    event.target.src = this.imagePlaceholderService.getProductPlaceholder();
  }

  onImageLoad(event: any): void {
    const img = event.target;
    const productId = this.getProductIdFromImage(img);
    if (productId) {
      this.loadedImages[productId] = true;
      img.classList.remove('loading');
    }
  }

  isImageLoaded(productId: number): boolean {
    return this.loadedImages[productId] || false;
  }

  private getProductIdFromImage(img: HTMLImageElement): number | null {
    // Find the product ID from the image's parent elements
    let element = img.parentElement;
    while (element) {
      const productCard = element.closest('mat-grid-tile');
      if (productCard) {
        const productIndex = Array.from(productCard.parentElement?.children || []).indexOf(productCard);
        return this.products[productIndex]?.productId || null;
      }
      element = element.parentElement;
    }
    return null;
  }

  getStockColor(stockQty: number): string {
    if (stockQty === 0) return 'warn';
    if (stockQty < 10) return 'accent';
    return 'primary';
  }

  getStockText(stockQty: number): string {
    if (stockQty === 0) return 'Out of Stock';
    if (stockQty < 10) return `Low Stock (${stockQty})`;
    return `In Stock (${stockQty})`;
  }
}