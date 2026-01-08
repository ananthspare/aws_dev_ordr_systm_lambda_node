import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { Observable } from 'rxjs';
import { ProductService } from '../../../core/services/product.service';
import { CartService } from '../../../core/services/cart.service';
import { AuthService } from '../../../core/services/auth.service';
import { ImagePlaceholderService } from '../../../core/services/image-placeholder.service';
import { Product } from '../../../core/models/product.model';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatChipsModule,
    MatFormFieldModule,
    MatInputModule
  ],
  template: `
    <div class="product-detail-container">
      <div class="back-button">
        <button mat-button (click)="goBack()">
          <mat-icon>arrow_back</mat-icon>
          Back to Products
        </button>
      </div>

      <div *ngIf="isLoading" class="loading-container">
        <mat-spinner></mat-spinner>
        <p>Loading product details...</p>
      </div>

      <div *ngIf="!isLoading && !product" class="error-state">
        <mat-icon>error_outline</mat-icon>
        <h2>Product not found</h2>
        <p>The product you're looking for doesn't exist.</p>
        <button mat-raised-button color="primary" routerLink="/products">
          Browse Products
        </button>
      </div>

      <div *ngIf="!isLoading && product" class="product-detail">
        <div class="product-images">
          <div class="main-image">
            <img [src]="getProductImage(product)" 
                 [alt]="product.name" 
                 (error)="onImageError($event)"
                 (load)="onImageLoad($event)"
                 [class.loading]="!imageLoaded">
            <div class="image-placeholder" *ngIf="!imageLoaded">
              <mat-icon>image</mat-icon>
              <p>Loading image...</p>
            </div>
          </div>
        </div>

        <div class="product-info">
          <mat-card>
            <mat-card-header>
              <mat-card-title>{{ product.name }}</mat-card-title>
              <mat-card-subtitle>Product ID: {{ product.productId }}</mat-card-subtitle>
            </mat-card-header>

            <mat-card-content>
              <div class="price-section">
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

              <div class="description-section">
                <h3>Description</h3>
                <p>{{ product.description || 'No description available.' }}</p>
              </div>

<!--              <div class="quantity-section" *ngIf="product.stockQty > 0">-->
<!--                <mat-form-field appearance="outline">-->
<!--                  <mat-label>Quantity</mat-label>-->
<!--                  <input matInput -->
<!--                         type="number" -->
<!--                         [(ngModel)]="selectedQuantity"-->
<!--                         [min]="1" -->
<!--                         [max]="product.stockQty"-->
<!--                         placeholder="1">-->
<!--                </mat-form-field>-->
<!--              </div>-->
            </mat-card-content>

            <mat-card-actions>
              <!-- Show quantity controls if item is in cart, otherwise show Add to Cart -->
              <div *ngIf="getProductQuantityInCart(product.productId) | async as quantity; else addToCartSection" 
                   class="quantity-section-inline">
                <div class="quantity-controls-inline">
                  <button mat-icon-button 
                          color="primary"
                          [disabled]="isUpdatingQuantity"
                          (click)="updateCartQuantity(quantity - 1)">
                    <mat-icon>remove</mat-icon>
                  </button>
                  
                  <span class="quantity-display-inline">{{ quantity }} in cart</span>
                  
                  <button mat-icon-button 
                          color="primary"
                          [disabled]="isUpdatingQuantity || quantity >= product.stockQty"
                          (click)="updateCartQuantity(quantity + 1)">
                    <mat-icon>add</mat-icon>
                  </button>
                </div>
              </div>
              
              <ng-template #addToCartSection>
                <div class="quantity-section" *ngIf="product.stockQty > 0">
                  <mat-form-field appearance="outline">
                    <mat-label>Quantity</mat-label>
                    <input matInput 
                           type="number" 
                           [(ngModel)]="selectedQuantity"
                           [min]="1" 
                           [max]="product.stockQty"
                           placeholder="1">
                  </mat-form-field>
                </div>
                
                <button mat-raised-button 
                        color="accent" 
                        [disabled]="product.stockQty === 0 || isAddingToCart"
                        (click)="addToCart()"
                        class="add-to-cart-btn">
                  <mat-spinner diameter="20" *ngIf="isAddingToCart"></mat-spinner>
                  <mat-icon *ngIf="!isAddingToCart">add_shopping_cart</mat-icon>
                  <span *ngIf="!isAddingToCart">Add to Cart</span>
                </button>
              </ng-template>

              <button mat-button color="primary" routerLink="/products">
                <mat-icon>arrow_back</mat-icon>
                Continue Shopping
              </button>
            </mat-card-actions>
          </mat-card>

          <mat-card class="product-details">
            <mat-card-header>
              <mat-card-title>Product Details</mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <div class="detail-row">
                <span class="label">Product ID:</span>
                <span class="value">{{ product.productId }}</span>
              </div>
              <div class="detail-row">
                <span class="label">Price:</span>
                <span class="value">\${{ product.price | number:'1.2-2' }}</span>
              </div>
              <div class="detail-row">
                <span class="label">Stock Quantity:</span>
                <span class="value">{{ product.stockQty }}</span>
              </div>
              <div class="detail-row">
                <span class="label">Created:</span>
                <span class="value">{{ product.createdAt | date:'medium' }}</span>
              </div>
            </mat-card-content>
          </mat-card>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .product-detail-container {
      padding: 20px;
      max-width: 1200px;
      margin: 0 auto;
    }

    .back-button {
      margin-bottom: 20px;
    }

    .loading-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 300px;
      gap: 20px;
    }

    .error-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 300px;
      color: #666;
      text-align: center;
    }

    .error-state mat-icon {
      font-size: 64px;
      width: 64px;
      height: 64px;
      margin-bottom: 16px;
    }

    .product-detail {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 40px;
      align-items: start;
    }

    .product-images {
      position: sticky;
      top: 20px;
    }

    .main-image {
      width: 100%;
      height: 400px;
      border-radius: 8px;
      overflow: hidden;
      background: #f5f5f5;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .main-image img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: opacity 0.3s ease;
    }

    .main-image img.loading {
      opacity: 0;
    }

    .image-placeholder {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      background: #f0f0f0;
      color: #999;
    }

    .image-placeholder mat-icon {
      font-size: 64px;
      width: 64px;
      height: 64px;
      margin-bottom: 8px;
    }

    .product-info {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .price-section {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;
      padding-bottom: 16px;
      border-bottom: 1px solid #e0e0e0;
    }

    .price {
      display: flex;
      align-items: baseline;
      font-weight: 600;
      color: #2e7d32;
    }

    .currency {
      font-size: 1.2em;
      margin-right: 4px;
    }

    .amount {
      font-size: 2em;
    }

    .description-section {
      margin-bottom: 24px;
    }

    .description-section h3 {
      margin: 0 0 12px 0;
      color: #333;
    }

    .description-section p {
      color: #666;
      line-height: 1.6;
    }

    .quantity-section {
      margin-bottom: 24px;
    }

    .quantity-section mat-form-field {
      width: 120px;
    }

    .add-to-cart-btn {
      min-width: 160px;
      height: 48px;
    }

    .quantity-controls-inline {
      display: flex;
      align-items: center;
      gap: 12px;
      background: rgba(63, 81, 181, 0.1);
      border-radius: 25px;
      padding: 8px 16px;
      margin-bottom: 12px;
    }

    .quantity-display-inline {
      font-weight: 600;
      color: #3f51b5;
      font-size: 1.1em;
    }

    .quantity-section-inline {
      display: flex;
      flex-direction: column;
      align-items: flex-start;
    }

    mat-card-actions {
      display: flex;
      gap: 12px;
      flex-wrap: wrap;
    }

    .product-details .detail-row {
      display: flex;
      justify-content: space-between;
      padding: 8px 0;
      border-bottom: 1px solid #f0f0f0;
    }

    .product-details .detail-row:last-child {
      border-bottom: none;
    }

    .detail-row .label {
      font-weight: 500;
      color: #666;
    }

    .detail-row .value {
      color: #333;
    }

    @media (max-width: 768px) {
      .product-detail-container {
        padding: 16px;
      }

      .product-detail {
        grid-template-columns: 1fr;
        gap: 20px;
      }

      .product-images {
        position: static;
      }

      .main-image {
        height: 300px;
      }

      .price {
        flex-direction: column;
        align-items: flex-start;
      }

      mat-card-actions {
        flex-direction: column;
      }

      .add-to-cart-btn {
        width: 100%;
      }
    }
  `]
})
export class ProductDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private productService = inject(ProductService);
  private cartService = inject(CartService);
  private authService = inject(AuthService);
  private snackBar = inject(MatSnackBar);
  private imagePlaceholderService = inject(ImagePlaceholderService);

  product: Product | null = null;
  isLoading = true;
  isAddingToCart = false;
  isUpdatingQuantity = false;
  selectedQuantity = 1;
  imageLoaded = false;

  ngOnInit(): void {
    const productId = this.route.snapshot.paramMap.get('id');
    if (productId) {
      this.loadProduct(+productId);
    } else {
      this.isLoading = false;
    }
  }

  loadProduct(productId: number): void {
    this.isLoading = true;
    this.imageLoaded = false;
    this.productService.getProductById(productId).subscribe({
      next: (product) => {
        this.product = product;
        this.isLoading = false;
      },
      error: (error: any) => {
        this.isLoading = false;
        console.error('Error loading product:', error);
      }
    });
  }

  addToCart(): void {
    if (!this.product) return;

    if (!this.authService.isAuthenticated()) {
      this.snackBar.open('Please login to add items to cart', 'Login', { duration: 3000 });
      return;
    }

    if (this.selectedQuantity < 1 || this.selectedQuantity > this.product.stockQty) {
      this.snackBar.open('Please select a valid quantity', 'Close', { duration: 3000 });
      return;
    }

    this.isAddingToCart = true;
    
    this.cartService.addToCart(this.product.productId, this.selectedQuantity).subscribe({
      next: () => {
        this.isAddingToCart = false;
        this.snackBar.open(
          `${this.selectedQuantity} x ${this.product!.name} added to cart!`, 
          'Close', 
          { duration: 2000 }
        );
      },
      error: (error: any) => {
        this.isAddingToCart = false;
        const message = error.error?.message || 'Failed to add item to cart';
        this.snackBar.open(message, 'Close', { duration: 3000 });
      }
    });
  }

  updateCartQuantity(newQuantity: number): void {
    if (!this.product || !this.authService.isAuthenticated()) {
      return;
    }

    if (newQuantity <= 0) {
      // Remove item from cart
      this.cartService.removeFromCart(this.product.productId).subscribe({
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

    this.isUpdatingQuantity = true;
    
    this.cartService.updateCartItem(this.product.productId, newQuantity).subscribe({
      next: () => {
        this.isUpdatingQuantity = false;
      },
      error: (error: any) => {
        this.isUpdatingQuantity = false;
        const message = error.error?.message || 'Failed to update quantity';
        this.snackBar.open(message, 'Close', { duration: 3000 });
      }
    });
  }

  getProductQuantityInCart(productId: number): Observable<number> {
    return this.cartService.getProductQuantityInCart(productId);
  }

  goBack(): void {
    this.router.navigate(['/products']);
  }

  getProductImage(product: Product): string {
    // Use the imageUrl from the product if available, otherwise use placeholder
    if (product.imageUrl && product.imageUrl.trim() !== '') {
      return product.imageUrl;
    }
    // Use local placeholder service instead of external API
    return this.imagePlaceholderService.getPlaceholder('product-detail', product.name);
  }

  onImageError(event: any): void {
    // Use local placeholder instead of external API
    event.target.src = this.imagePlaceholderService.getProductPlaceholder();
    this.imageLoaded = true;
  }

  onImageLoad(event: any): void {
    this.imageLoaded = true;
    event.target.classList.remove('loading');
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