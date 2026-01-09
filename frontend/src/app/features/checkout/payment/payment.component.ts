import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { OrderService } from '../../../core/services/order.service';
import { CartService } from '../../../core/services/cart.service';
import { CheckoutService } from '../../../core/services/checkout.service';

@Component({
  selector: 'app-payment',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatSnackBarModule,
    MatProgressSpinnerModule
  ],
  template: `
    <div class="payment-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>Payment Information</mat-card-title>
          <mat-card-subtitle>Complete your order</mat-card-subtitle>
        </mat-card-header>

        <mat-card-content>
          <form [formGroup]="paymentForm" (ngSubmit)="onSubmit()">
            <div class="form-row">
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Payment Method</mat-label>
                <mat-select formControlName="paymentMethod" required>
                  <mat-option value="MOCK">Mock Payment</mat-option>
                  <mat-option value="CARD">Credit/Debit Card</mat-option>
                  <mat-option value="PAYPAL">PayPal</mat-option>
                  <mat-option value="BANK_TRANSFER">Bank Transfer</mat-option>
                  <mat-option value="UPI">UPI</mat-option>
                  <mat-option value="CASH_ON_DELIVERY">Cash on Delivery</mat-option>
                </mat-select>
                <mat-error *ngIf="paymentForm.get('paymentMethod')?.hasError('required')">
                  Payment method is required
                </mat-error>
              </mat-form-field>
            </div>

            <div class="form-row">
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Currency</mat-label>
                <mat-select formControlName="currency" required>
                  <mat-option value="USD">USD - US Dollar</mat-option>
                  <mat-option value="CAD">CAD - Canadian Dollar</mat-option>
                  <mat-option value="GBP">GBP - British Pound</mat-option>
                </mat-select>
                <mat-error *ngIf="paymentForm.get('currency')?.hasError('required')">
                  Currency is required
                </mat-error>
              </mat-form-field>
            </div>

            <div class="order-summary" *ngIf="cartItems.length > 0">
              <h3>Order Summary</h3>
              <div class="item" *ngFor="let item of cartItems">
                <span>{{item.productName}} x {{item.quantity}}</span>
                <span>\${{(item.unitPrice * item.quantity).toFixed(2)}}</span>
              </div>
              <div class="total">
                <strong>Total: \${{totalAmount.toFixed(2)}}</strong>
              </div>
            </div>
          </form>
        </mat-card-content>

        <mat-card-actions>
          <button mat-button type="button" (click)="goBack()">Back</button>
          <button mat-raised-button color="primary" 
                  (click)="onSubmit()" 
                  [disabled]="paymentForm.invalid || isLoading || cartItems.length === 0">
            <mat-spinner *ngIf="isLoading" diameter="20"></mat-spinner>
            Place Order
          </button>
        </mat-card-actions>
      </mat-card>
    </div>
  `,
  styles: [`
    .payment-container {
      max-width: 600px;
      margin: 20px auto;
      padding: 20px;
    }

    .form-row {
      display: flex;
      gap: 16px;
      margin-bottom: 16px;
    }

    .full-width {
      width: 100%;
    }

    .order-summary {
      margin: 20px 0;
      padding: 16px;
      border: 1px solid #ddd;
      border-radius: 4px;
    }

    .item {
      display: flex;
      justify-content: space-between;
      margin-bottom: 8px;
    }

    .total {
      border-top: 1px solid #ddd;
      padding-top: 8px;
      margin-top: 8px;
      text-align: right;
    }

    mat-card-actions {
      display: flex;
      justify-content: space-between;
      padding: 16px;
    }

    mat-spinner {
      margin-right: 8px;
    }
  `]
})
export class PaymentComponent implements OnInit {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);
  private orderService = inject(OrderService);
  private cartService = inject(CartService);
  private checkoutService = inject(CheckoutService);

  paymentForm!: FormGroup;
  isLoading = false;
  cartItems: any[] = [];
  totalAmount = 0;

  ngOnInit(): void {
    this.initForm();
    this.loadCartItems();
  }

  private initForm(): void {
    this.paymentForm = this.fb.group({
      paymentMethod: ['MOCK', Validators.required],
      currency: ['USD', Validators.required]
    });
  }

  private loadCartItems(): void {
    this.cartService.getCart().subscribe({
      next: (cart) => {
        this.cartItems = cart.items;
        this.totalAmount = cart.totalAmount;
      },
      error: (error) => {
        console.error('Error loading cart items:', error);
        this.snackBar.open('Failed to load cart items', 'Close', { duration: 3000 });
      }
    });
  }

  onSubmit(): void {
    if (this.paymentForm.valid && this.cartItems.length > 0) {
      this.isLoading = true;
      
      const customerId = this.checkoutService.getCustomerId();
      const shippingAddress = this.checkoutService.getShippingAddress();
      
      if (!customerId || !shippingAddress) {
        this.snackBar.open('Missing checkout information', 'Close', { duration: 3000 });
        this.router.navigate(['/checkout/shipping']);
        return;
      }

      const checkoutRequest = {
        customerId: customerId,
        countryCode: shippingAddress.countryCode,
        orderItems: this.cartItems.map(item => ({
          productId: item.productId,
          quantity: item.quantity,
          unitPrice: item.unitPrice
        })),
        paymentMethod: this.paymentForm.value.paymentMethod,
        currency: this.paymentForm.value.currency,
        shippingAddressId: shippingAddress.shippingAddressId
      };

      this.orderService.checkout(checkoutRequest).subscribe({
        next: (response) => {
          this.isLoading = false;
          this.snackBar.open('Order placed successfully!', 'Close', { duration: 3000 });
          this.checkoutService.clearCheckoutData();
          // Refresh cart to update the cart count in header
          this.cartService.getCart().subscribe();
          this.router.navigate(['/orders']);
        },
        error: (error) => {
          this.isLoading = false;
          console.error('Error placing order:', error);
          this.snackBar.open('Failed to place order', 'Close', { duration: 3000 });
        }
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/checkout/shipping']);
  }
}