import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap, map } from 'rxjs';
import { Cart, CartItem, AddToCartRequest, UpdateCartItemRequest } from '../models/cart.model';
import { ApiResponse } from '../models/api-response.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private http = inject(HttpClient);
  private cartSubject = new BehaviorSubject<Cart>(this.getInitialCart());
  
  public cart$ = this.cartSubject.asObservable();

  constructor() {
    // Load cart when service initializes
    this.loadCart();
  }

  /**
   * Get cart from backend
   */
  getCart(): Observable<Cart> {
    return this.http.get<ApiResponse<Cart>>(`${environment.apiUrl}/cart`)
      .pipe(
        map(response => {
          if (response.success && response.data) {
            this.cartSubject.next(response.data);
            return response.data;
          }
          return this.getInitialCart();
        })
      );
  }

  /**
   * Load cart from backend without updating subject (internal use)
   */
  private loadCartFromBackend(): Observable<Cart> {
    return this.http.get<ApiResponse<Cart>>(`${environment.apiUrl}/cart`)
      .pipe(
        map(response => {
          if (response.success && response.data) {
            return response.data;
          }
          return this.getInitialCart();
        })
      );
  }

  /**
   * Add item to cart
   */
  addToCart(productId: number, quantity: number = 1): Observable<CartItem> {
    const request: AddToCartRequest = { productId, quantity };
    
    return this.http.post<ApiResponse<CartItem>>(`${environment.apiUrl}/cart/items`, request)
      .pipe(
        tap(() => {
          // Refresh cart after adding item
          this.loadCart();
        }),
        map(response => response.data!)
      );
  }

  /**
   * Update cart item quantity
   */
  updateCartItem(productId: number, quantity: number): Observable<CartItem> {
    const request: UpdateCartItemRequest = { quantity };
    
    return this.http.put<ApiResponse<CartItem>>(`${environment.apiUrl}/cart/items/${productId}`, request)
      .pipe(
        tap(() => {
          // Refresh cart after updating item
          this.loadCart();
        }),
        map(response => response.data!)
      );
  }

  /**
   * Remove item from cart
   */
  removeFromCart(productId: number): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${environment.apiUrl}/cart/items/${productId}`)
      .pipe(
        tap(() => {
          // Refresh cart after removing item
          this.loadCart();
        }),
        map(() => void 0)
      );
  }

  /**
   * Clear entire cart
   */
  clearCart(): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${environment.apiUrl}/cart`)
      .pipe(
        tap(() => {
          // Update local cart state
          this.cartSubject.next(this.getInitialCart());
        }),
        map(() => void 0)
      );
  }

  /**
   * Get cart item count
   */
  getCartItemCount(): Observable<number> {
    return this.http.get<ApiResponse<number>>(`${environment.apiUrl}/cart/count`)
      .pipe(
        map(response => response.data || 0)
      );
  }

  /**
   * Validate cart items
   */
  validateCart(): Observable<void> {
    return this.http.post<ApiResponse<void>>(`${environment.apiUrl}/cart/validate`, {})
      .pipe(
        tap(() => {
          // Refresh cart after validation (prices might have been updated)
          this.loadCart();
        }),
        map(() => void 0)
      );
  }

  /**
   * Get current cart value (synchronous)
   */
  getCurrentCart(): Cart {
    return this.cartSubject.value;
  }

  /**
   * Get cart total amount (synchronous)
   */
  getCartTotal(): number {
    return this.cartSubject.value.totalAmount;
  }

  /**
   * Get total items count (synchronous)
   */
  getTotalItemsCount(): number {
    return this.cartSubject.value.totalItems;
  }

  /**
   * Load cart from backend and update local state
   */
  private loadCart(): void {
    this.loadCartFromBackend().subscribe({
      next: (cart) => {
        this.cartSubject.next(cart);
      },
      error: (error) => {
        console.error('Error loading cart:', error);
        // Keep current cart state on error
      }
    });
  }

  /**
   * Check if a product is already in cart
   */
  isProductInCart(productId: number): Observable<boolean> {
    return this.cart$.pipe(
      map(cart => cart.items.some(item => item.productId === productId))
    );
  }

  /**
   * Get quantity of a specific product in cart
   */
  getProductQuantityInCart(productId: number): Observable<number> {
    return this.cart$.pipe(
      map(cart => {
        const item = cart.items.find(item => item.productId === productId);
        return item ? item.quantity : 0;
      })
    );
  }
  private getInitialCart(): Cart {
    return {
      customerId: 0,
      items: [],
      totalItems: 0,
      totalAmount: 0,
      lastUpdated: new Date().toISOString()
    };
  }
}