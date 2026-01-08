import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatChipsModule } from '@angular/material/chips';
import { MatTableModule } from '@angular/material/table';
import { OrderService } from '../../../core/services/order.service';
import { Order } from '../../../core/models/order.model';

@Component({
  selector: 'app-order-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatChipsModule,
    MatTableModule
  ],
  template: `
    <div class="orders-container">
      <div class="header">
        <h1>My Orders</h1>
        <button mat-button routerLink="/products">
          <mat-icon>arrow_back</mat-icon>
          Continue Shopping
        </button>
      </div>

      <div *ngIf="isLoading" class="loading-container">
        <mat-spinner></mat-spinner>
        <p>Loading your orders...</p>
      </div>

      <div *ngIf="!isLoading && orders.length === 0" class="empty-state">
        <mat-icon>receipt_long</mat-icon>
        <h2>No orders yet</h2>
        <p>Start shopping to see your orders here!</p>
        <button mat-raised-button color="primary" routerLink="/products">
          <mat-icon>shopping_bag</mat-icon>
          Shop Now
        </button>
      </div>

      <div *ngIf="!isLoading && orders.length > 0" class="orders-list">
        <mat-card *ngFor="let order of orders" class="order-card">
          <mat-card-header>
            <mat-card-title>Order #{{ order.orderId }}</mat-card-title>
            <mat-card-subtitle>
              Placed on {{ order.createdAt | date:'medium' }}
            </mat-card-subtitle>
          </mat-card-header>

          <mat-card-content>
            <div class="order-info">
              <div class="info-row">
                <span class="label">Status:</span>
                <mat-chip [color]="getStatusColor(order.status)" selected>
                  <mat-icon>{{ getStatusIcon(order.status) }}</mat-icon>
                  {{ order.status }}
                </mat-chip>
              </div>

              <div class="info-row">
                <span class="label">Total Amount:</span>
                <span class="amount">\${{ order.totalAmount | number:'1.2-2' }}</span>
              </div>

              <div class="info-row">
                <span class="label">Payment Status:</span>
                <mat-chip [color]="getPaymentStatusColor(order.paymentStatus)" selected>
                  <mat-icon>{{ getPaymentStatusIcon(order.paymentStatus) }}</mat-icon>
                  {{ order.paymentStatus || 'PENDING' }}
                </mat-chip>
              </div>

              <div class="info-row" *ngIf="order.countryCode">
                <span class="label">Country:</span>
                <span>{{ order.countryCode }}</span>
              </div>

              <div class="info-row" *ngIf="order.invoiceS3Path">
                <span class="label">Invoice:</span>
                <button mat-button color="primary" (click)="downloadInvoice(order)">
                  <mat-icon>download</mat-icon>
                  Download
                </button>
              </div>
            </div>

            <div class="order-items" *ngIf="order.items && order.items.length > 0">
              <h4>Items ({{ order.items.length }})</h4>
              <div class="items-list">
                <div *ngFor="let item of order.items" class="order-item">
                  <div class="item-info">
                    <span class="item-name">{{ item.productName || 'Product #' + item.productId }}</span>
                    <span class="item-details">
                      {{ item.quantity }} x \${{ item.unitPrice | number:'1.2-2' }}
                    </span>
                  </div>
                  <span class="item-total">\${{ (item.quantity * item.unitPrice) | number:'1.2-2' }}</span>
                </div>
              </div>
            </div>
          </mat-card-content>

          <mat-card-actions>
            <button mat-button color="primary" [routerLink]="['/orders', order.orderId]">
              <mat-icon>visibility</mat-icon>
              View Details
            </button>
            
            <button mat-button 
                    *ngIf="order.status === 'CREATED' || order.status === 'PENDING'"
                    (click)="cancelOrder(order)">
              <mat-icon>cancel</mat-icon>
              Cancel Order
            </button>
            
            <button mat-button 
                    *ngIf="order.status === 'DELIVERED'"
                    (click)="reorder(order)">
              <mat-icon>refresh</mat-icon>
              Reorder
            </button>
          </mat-card-actions>
        </mat-card>
      </div>
    </div>
  `,
  styles: [`
    .orders-container {
      padding: 20px;
      max-width: 1000px;
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

    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 400px;
      color: #666;
      text-align: center;
    }

    .empty-state mat-icon {
      font-size: 80px;
      width: 80px;
      height: 80px;
      margin-bottom: 20px;
    }

    .orders-list {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .order-card {
      transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
    }

    .order-card:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    }

    .order-info {
      display: flex;
      flex-direction: column;
      gap: 12px;
      margin-bottom: 20px;
    }

    .info-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .label {
      font-weight: 500;
      color: #666;
      min-width: 120px;
    }

    .amount {
      font-weight: 600;
      color: #2e7d32;
      font-size: 1.1em;
    }

    .order-items {
      border-top: 1px solid #e0e0e0;
      padding-top: 16px;
    }

    .order-items h4 {
      margin: 0 0 12px 0;
      color: #333;
    }

    .items-list {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .order-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 12px;
      background: #f9f9f9;
      border-radius: 4px;
    }

    .item-info {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .item-name {
      font-weight: 500;
      color: #333;
    }

    .item-details {
      font-size: 0.9em;
      color: #666;
    }

    .item-total {
      font-weight: 600;
      color: #2e7d32;
    }

    mat-card-actions {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }

    @media (max-width: 768px) {
      .orders-container {
        padding: 16px;
      }

      .header {
        flex-direction: column;
        gap: 16px;
        align-items: flex-start;
      }

      .info-row {
        flex-direction: column;
        align-items: flex-start;
        gap: 4px;
      }

      .label {
        min-width: auto;
      }

      mat-card-actions {
        flex-direction: column;
      }
    }
  `]
})
export class OrderListComponent implements OnInit {
  private orderService = inject(OrderService);
  private snackBar = inject(MatSnackBar);

  orders: Order[] = [];
  isLoading = true;

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.isLoading = true;
    this.orderService.getCustomerOrders().subscribe({
      next: (orders) => {
        this.orders = orders;
        this.isLoading = false;
      },
      error: (error: any) => {
        this.isLoading = false;
        this.snackBar.open('Failed to load orders', 'Close', { duration: 3000 });
        console.error('Error loading orders:', error);
      }
    });
  }

  getStatusColor(status: string): string {
    switch (status?.toUpperCase()) {
      case 'CREATED':
      case 'PENDING':
        return 'accent';
      case 'PROCESSING':
        return 'primary';
      case 'SHIPPED':
        return 'primary';
      case 'DELIVERED':
        return 'primary';
      case 'CANCELLED':
        return 'warn';
      default:
        return 'basic';
    }
  }

  getStatusIcon(status: string): string {
    switch (status?.toUpperCase()) {
      case 'CREATED':
      case 'PENDING':
        return 'schedule';
      case 'PROCESSING':
        return 'autorenew';
      case 'SHIPPED':
        return 'local_shipping';
      case 'DELIVERED':
        return 'check_circle';
      case 'CANCELLED':
        return 'cancel';
      default:
        return 'help';
    }
  }

  getPaymentStatusColor(status: string): string {
    switch (status?.toUpperCase()) {
      case 'PAID':
        return 'primary';
      case 'PENDING':
        return 'accent';
      case 'FAILED':
        return 'warn';
      default:
        return 'basic';
    }
  }

  getPaymentStatusIcon(status: string): string {
    switch (status?.toUpperCase()) {
      case 'PAID':
        return 'check_circle';
      case 'PENDING':
        return 'schedule';
      case 'FAILED':
        return 'error';
      default:
        return 'help';
    }
  }

  downloadInvoice(order: Order): void {
    if (order.invoiceS3Path) {
      // In a real app, you'd call a service to get the download URL
      this.snackBar.open('Invoice download functionality coming soon!', 'Close', { duration: 3000 });
    }
  }

  cancelOrder(order: Order): void {
    this.snackBar.open('Order cancellation functionality coming soon!', 'Close', { duration: 3000 });
  }

  reorder(order: Order): void {
    this.snackBar.open('Reorder functionality coming soon!', 'Close', { duration: 3000 });
  }
}