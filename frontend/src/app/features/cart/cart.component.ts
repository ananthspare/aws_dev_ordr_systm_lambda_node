import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatListModule } from '@angular/material/list';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTooltipModule } from '@angular/material/tooltip';
import { FormsModule } from '@angular/forms';
import { CartService } from '../../core/services/cart.service';
import { CheckoutService } from '../../core/services/checkout.service';
import { AuthService } from '../../core/services/auth.service';
import { ImagePlaceholderService } from '../../core/services/image-placeholder.service';
import { Cart, CartItem } from '../../core/models/cart.model';

@Component({
  selector: 'app-cart',
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
    MatListModule,
    MatDividerModule,
    MatFormFieldModule,
    MatInputModule,
    MatTooltipModule
  ],
  template: `
    <div class="cart-container">
      <div class="header">
        <h1>Shopping Cart</h1>
        <button mat-button routerLink="/products">
          <mat-icon>arrow_back</mat-icon>
          Continue Shopping
        </button>
      </div>

      <div *ngIf="isLoading" class="loading-container">
        <mat-spinner></mat-spinner>
        <p>Loading your cart...</p>
      </div>

      <div *ngIf="!isLoading && (!cart || cart.items.length === 0)" class="empty-cart">
        <mat-icon>shopping_cart</mat-icon>
        <h2>Your cart is empty</h2>
        <p>Add some products to get started!</p>
        <button mat-raised-button color="primary" routerLink="/products">
          <mat-icon>shopping_bag</mat-icon>
          Shop Now
        </button>
      </div>

      <div *ngIf="!isLoading && cart && cart.items.length > 0" class="cart-content">
        <div class="cart-items">
          <mat-card>
            <mat-card-header>
              <mat-card-title>Cart Items ({{ cart.totalItems }})</mat-card-title>
            </mat-card-header>
            
            <mat-card-content>
              <mat-list>
                <div *ngFor="let item of cart.items; let last = last">
                  <div class="cart-item">
                    <div class="item-image">
                      <img [src]="getProductImage(item)" 
                           [alt]="item.productName" 
                           (error)="onImageError($event)">
                    </div>
                    
                    <div class="item-details">
                      <h3>{{ item.productName }}</h3>
                      <p class="item-price">\${{ item.unitPrice | number:'1.2-2' }} each</p>
                    </div>
                    
                    <div class="item-quantity">
                      <button mat-icon-button 
                              [disabled]="isUpdating[item.productId]"
                              (click)="updateQuantity(item, item.quantity - 1)">
                        <mat-icon>remove</mat-icon>
                      </button>
                      
                      <mat-form-field appearance="outline" class="quantity-input">
                        <input matInput 
                               type="number" 
                               [value]="item.quantity"
                               [min]="1"
                               [disabled]="isUpdating[item.productId]"
                               (blur)="onQuantityChange(item, $event)">
                      </mat-form-field>
                      
                      <button mat-icon-button 
                              [disabled]="isUpdating[item.productId]"
                              (click)="updateQuantity(item, item.quantity + 1)">
                        <mat-icon>add</mat-icon>
                      </button>
                    </div>
                    
                    <div class="item-total">
                      <span class="total-price">\${{ item.totalPrice | number:'1.2-2' }}</span>
                    </div>
                    
                    <div class="item-actions">
                      <button mat-icon-button 
                              color="warn"
                              [disabled]="isUpdating[item.productId]"
                              (click)="removeItem(item)"
                              matTooltip="Remove from cart">
                        <mat-spinner diameter="20" *ngIf="isUpdating[item.productId]"></mat-spinner>
                        <mat-icon *ngIf="!isUpdating[item.productId]">delete</mat-icon>
                      </button>
                    </div>
                  </div>
                  
                  <mat-divider *ngIf="!last"></mat-divider>
                </div>
              </mat-list>
            </mat-card-content>
          </mat-card>
        </div>

        <div class="cart-summary">
          <mat-card>
            <mat-card-header>
              <mat-card-title>Order Summary</mat-card-title>
            </mat-card-header>
            
            <mat-card-content>
              <div class="summary-row">
                <span>Subtotal ({{ cart.totalItems }} items):</span>
                <span>\${{ cart.totalAmount | number:'1.2-2' }}</span>
              </div>
              
              <div class="summary-row">
                <span>Shipping:</span>
                <span>Free</span>
              </div>
              
              <div class="summary-row">
                <span>Tax:</span>
                <span>\${{ getTax() | number:'1.2-2' }}</span>
              </div>
              
              <mat-divider></mat-divider>
              
              <div class="summary-row total">
                <span>Total:</span>
                <span>\${{ getTotal() | number:'1.2-2' }}</span>
              </div>
            </mat-card-content>
            
            <mat-card-actions>
              <button mat-raised-button 
                      color="primary" 
                      class="checkout-btn"
                      [disabled]="isCheckingOut"
                      (click)="proceedToCheckout()">
                <mat-spinner diameter="20" *ngIf="isCheckingOut"></mat-spinner>
                <mat-icon *ngIf="!isCheckingOut">payment</mat-icon>
                <span *ngIf="!isCheckingOut">Proceed to Checkout</span>
              </button>
              
              <button mat-button (click)="clearCart()" [disabled]="isClearingCart">
                <mat-spinner diameter="20" *ngIf="isClearingCart"></mat-spinner>
                <mat-icon *ngIf="!isClearingCart">clear</mat-icon>
                <span *ngIf="!isClearingCart">Clear Cart</span>
              </button>
            </mat-card-actions>
          </mat-card>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .cart-container {
      padding: 20px;
      max-width: 1200px;
      margin: 0 auto;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 30px;
    }

    .header h1 {
      margin: 0;
      color: #333;
    }

    .loading-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 300px;
      gap: 20px;
    }

    .empty-cart {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 400px;
      color: #666;
      text-align: center;
    }

    .empty-cart mat-icon {
      font-size: 80px;
      width: 80px;
      height: 80px;
      margin-bottom: 20px;
    }

    .cart-content {
      display: flex;
      flex-direction: column;
      gap: 30px;
    }

    .cart-items {
      flex: 1;
    }

    .cart-item {
      display: grid;
      grid-template-columns: 80px 1fr auto auto auto;
      grid-template-areas: 
        "image details quantity total actions";
      align-items: center;
      padding: 20px 0;
      gap: 16px;
      min-height: 100px;
    }

    .item-image {
      grid-area: image;
      width: 80px;
      height: 80px;
      border-radius: 8px;
      overflow: hidden;
      background: #f5f5f5;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }

    .item-image img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .item-details {
      grid-area: details;
      display: flex;
      flex-direction: column;
      gap: 4px;
      min-width: 0;
    }

    .item-details h3 {
      margin: 0;
      font-size: 1.1em;
      color: #333;
      font-weight: 500;
    }

    .item-price {
      margin: 0;
      color: #666;
      font-size: 0.9em;
    }

    .item-quantity {
      grid-area: quantity;
      display: flex;
      align-items: center;
      gap: 8px;
      background: #f5f5f5;
      border-radius: 8px;
      padding: 4px;
    }

    .quantity-input {
      width: 60px;
    }

    .quantity-input input {
      text-align: center;
      font-weight: 500;
    }

    .item-total {
      grid-area: total;
      text-align: right;
      min-width: 80px;
    }

    .total-price {
      font-weight: 600;
      color: #2e7d32;
      font-size: 1.1em;
    }

    .item-actions {
      grid-area: actions;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .cart-summary {
      position: sticky;
      top: 20px;
      max-width: 400px;
      margin: 0 auto;
    }

    @media (min-width: 1024px) {
      .cart-content {
        display: grid;
        grid-template-columns: 2fr 1fr;
        gap: 30px;
        align-items: start;
      }

      .cart-summary {
        margin: 0;
      }
    }

    .summary-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 0;
    }

    .summary-row.total {
      font-weight: 600;
      font-size: 1.2em;
      color: #2e7d32;
      padding: 16px 0 8px 0;
    }

    .checkout-btn {
      width: 100%;
      height: 48px;
      margin-bottom: 8px;
    }

    @media (max-width: 768px) {
      .cart-container {
        padding: 16px;
      }

      .header {
        flex-direction: column;
        gap: 16px;
        align-items: flex-start;
      }

      .cart-item {
        grid-template-columns: 60px 1fr;
        grid-template-areas: 
          "image details"
          "quantity quantity"
          "total actions";
        gap: 12px;
        padding: 16px 0;
      }

      .item-image {
        width: 60px;
        height: 60px;
      }

      .item-quantity {
        justify-content: center;
        margin: 8px 0;
      }

      .item-total {
        text-align: left;
      }

      .item-actions {
        justify-content: flex-end;
      }

      .cart-summary {
        position: static;
        max-width: none;
      }
    }
  `]
})
export class CartComponent implements OnInit {
  private cartService = inject(CartService);
  private checkoutService = inject(CheckoutService);
  private authService = inject(AuthService);
  private snackBar = inject(MatSnackBar);
  private imagePlaceholderService = inject(ImagePlaceholderService);
  private router = inject(Router);

  cart: Cart | null = null;
  isLoading = true;
  isUpdating: { [key: number]: boolean } = {};
  isCheckingOut = false;
  isClearingCart = false;

  ngOnInit(): void {
    this.loadCart();
  }

  loadCart(): void {
    this.isLoading = true;
    this.cartService.getCart().subscribe({
      next: (cart) => {
        this.cart = cart;
        this.isLoading = false;
      },
      error: (error: any) => {
        this.isLoading = false;
        this.snackBar.open('Failed to load cart', 'Close', { duration: 3000 });
        console.error('Error loading cart:', error);
      }
    });
  }

  updateQuantity(item: CartItem, newQuantity: number): void {
    if (newQuantity < 1) {
      this.removeItem(item);
      return;
    }

    this.isUpdating[item.productId] = true;
    
    this.cartService.updateCartItem(item.productId, newQuantity).subscribe({
      next: () => {
        this.isUpdating[item.productId] = false;
        this.loadCart(); // Reload to get updated totals
      },
      error: (error: any) => {
        this.isUpdating[item.productId] = false;
        const message = error.error?.message || 'Failed to update quantity';
        this.snackBar.open(message, 'Close', { duration: 3000 });
      }
    });
  }

  onQuantityChange(item: CartItem, event: any): void {
    const newQuantity = parseInt(event.target.value);
    if (newQuantity && newQuantity !== item.quantity) {
      this.updateQuantity(item, newQuantity);
    }
  }

  removeItem(item: CartItem): void {
    this.isUpdating[item.productId] = true;
    
    this.cartService.removeFromCart(item.productId).subscribe({
      next: () => {
        this.isUpdating[item.productId] = false;
        this.snackBar.open(`${item.productName} removed from cart`, 'Close', { duration: 2000 });
        this.loadCart();
      },
      error: (error: any) => {
        this.isUpdating[item.productId] = false;
        const message = error.error?.message || 'Failed to remove item';
        this.snackBar.open(message, 'Close', { duration: 3000 });
      }
    });
  }

  clearCart(): void {
    this.isClearingCart = true;
    
    this.cartService.clearCart().subscribe({
      next: () => {
        this.isClearingCart = false;
        this.snackBar.open('Cart cleared', 'Close', { duration: 2000 });
        this.loadCart();
      },
      error: (error: any) => {
        this.isClearingCart = false;
        const message = error.error?.message || 'Failed to clear cart';
        this.snackBar.open(message, 'Close', { duration: 3000 });
      }
    });
  }

  proceedToCheckout(): void {
    if (!this.cart || this.cart.items.length === 0) {
      this.snackBar.open('Your cart is empty', 'Close', { duration: 3000 });
      return;
    }

    // Navigate to shipping address page
    this.router.navigate(['/checkout/shipping']);
  }

  getTax(): number {
    return this.cart ? this.cart.totalAmount * 0.08 : 0; // 8% tax
  }

  getTotal(): number {
    return this.cart ? this.cart.totalAmount + this.getTax() : 0;
  }

  getProductImage(item: CartItem): string {
    // Use the imageUrl from the cart item if available, otherwise use placeholder
    if (item.imageUrl && item.imageUrl.trim() !== '') {
      return item.imageUrl;
    }
    // Use local placeholder service instead of external API
    return this.imagePlaceholderService.getPlaceholder('cart', item.productName);
  }

  onImageError(event: any): void {
    // Use local placeholder instead of external API
    event.target.src = this.imagePlaceholderService.getCartPlaceholder();
  }
}