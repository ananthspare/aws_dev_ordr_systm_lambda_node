import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatListModule } from '@angular/material/list';
import { OrderService } from '../../../core/services/order.service';
import { Order, OrderStatus } from '../../../core/models/order.model';

@Component({
  selector: 'app-order-detail',
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
    MatDividerModule,
    MatListModule
  ],
  template: `
    <div class="order-detail-container">
      <div class="back-button">
        <button mat-button (click)="goBack()">
          <mat-icon>arrow_back</mat-icon>
          Back to Orders
        </button>
      </div>

      <div *ngIf="isLoading" class="loading-container">
        <mat-spinner></mat-spinner>
        <p>Loading order details...</p>
      </div>

      <div *ngIf="!isLoading && !order" class="error-state">
        <mat-icon>error_outline</mat-icon>
        <h2>Order not found</h2>
        <p>The order you're looking for doesn't exist.</p>
        <button mat-raised-button color="primary" routerLink="/orders">
          View All Orders
        </button>
      </div>

      <div *ngIf="!isLoading && order" class="order-detail">
        <div class="order-header">
          <mat-card>
            <mat-card-header>
              <mat-card-title>Order #{{ order.orderId }}</mat-card-title>
              <mat-card-subtitle>
                Placed on {{ order.createdAt | date:'full' }}
              </mat-card-subtitle>
            </mat-card-header>

            <mat-card-content>
              <div class="status-section">
                <div class="status-item">
                  <span class="label">Order Status:</span>
                  <mat-chip [color]="getStatusColor(order.status)" selected>
                    <mat-icon>{{ getStatusIcon(order.status) }}</mat-icon>
                    {{ order.status }}
                  </mat-chip>
                </div>

                <div class="status-item">
                  <span class="label">Payment Status:</span>
                  <mat-chip [color]="getPaymentStatusColor(order.paymentStatus)" selected>
                    <mat-icon>{{ getPaymentStatusIcon(order.paymentStatus) }}</mat-icon>
                    {{ order.paymentStatus || 'PENDING' }}
                  </mat-chip>
                </div>
              </div>

              <mat-divider></mat-divider>

              <div class="order-info">
                <div class="info-row">
                  <span class="label">Order ID:</span>
                  <span class="value">{{ order.orderId }}</span>
                </div>
                
                <div class="info-row">
                  <span class="label">Customer ID:</span>
                  <span class="value">{{ order.customerId }}</span>
                </div>
                
                <div class="info-row" *ngIf="order.countryCode">
                  <span class="label">Country:</span>
                  <span class="value">{{ order.countryCode }}</span>
                </div>
                
                <div class="info-row">
                  <span class="label">Total Amount:</span>
                  <span class="value amount">\${{ order.totalAmount | number:'1.2-2' }}</span>
                </div>
                
                <div class="info-row" *ngIf="order.invoiceS3Path">
                  <span class="label">Invoice:</span>
                  <button mat-button color="primary" (click)="downloadInvoice()">
                    <mat-icon>download</mat-icon>
                    Download Invoice
                  </button>
                </div>
              </div>
            </mat-card-content>

            <mat-card-actions>
              <button mat-button 
                      *ngIf="order.status === OrderStatus.CREATED || order.status === OrderStatus.PENDING"
                      (click)="cancelOrder()">
                <mat-icon>cancel</mat-icon>
                Cancel Order
              </button>
              
              <button mat-button 
                      *ngIf="order.status === 'DELIVERED'"
                      (click)="reorder()">
                <mat-icon>refresh</mat-icon>
                Reorder
              </button>
              
              <button mat-button (click)="trackOrder()" *ngIf="order.status === 'SHIPPED'">
                <mat-icon>track_changes</mat-icon>
                Track Order
              </button>
            </mat-card-actions>
          </mat-card>
        </div>

        <div class="order-items" *ngIf="order.items && order.items.length > 0">
          <mat-card>
            <mat-card-header>
              <mat-card-title>Order Items ({{ order.items.length }})</mat-card-title>
            </mat-card-header>

            <mat-card-content>
              <mat-list>
                <div *ngFor="let item of order.items; let last = last">
                  <mat-list-item class="order-item">
                    <div class="item-image">
                      <img [src]="getProductImage(item)" 
                           [alt]="item.productName || 'Product'" 
                           (error)="onImageError($event)">
                    </div>
                    
                    <div class="item-details">
                      <h3>{{ item.productName || 'Product #' + item.productId }}</h3>
                      <p class="item-id">Product ID: {{ item.productId }}</p>
                      <p class="item-price">Unit Price: \${{ item.unitPrice | number:'1.2-2' }}</p>
                    </div>
                    
                    <div class="item-quantity">
                      <span class="quantity-label">Quantity:</span>
                      <span class="quantity-value">{{ item.quantity }}</span>
                    </div>
                    
                    <div class="item-total">
                      <span class="total-label">Total:</span>
                      <span class="total-value">\${{ (item.quantity * item.unitPrice) | number:'1.2-2' }}</span>
                    </div>
                  </mat-list-item>
                  
                  <mat-divider *ngIf="!last"></mat-divider>
                </div>
              </mat-list>

              <div class="order-summary">
                <mat-divider></mat-divider>
                <div class="summary-row">
                  <span>Subtotal:</span>
                  <span>\${{ getSubtotal() | number:'1.2-2' }}</span>
                </div>
                <div class="summary-row">
                  <span>Tax:</span>
                  <span>\${{ getTax() | number:'1.2-2' }}</span>
                </div>
                <div class="summary-row">
                  <span>Shipping:</span>
                  <span>Free</span>
                </div>
                <div class="summary-row total">
                  <span>Total:</span>
                  <span>\${{ order.totalAmount | number:'1.2-2' }}</span>
                </div>
              </div>
            </mat-card-content>
          </mat-card>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .order-detail-container {
      padding: 20px;
      max-width: 1000px;
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

    .order-detail {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .status-section {
      display: flex;
      gap: 20px;
      margin-bottom: 20px;
      flex-wrap: wrap;
    }

    .status-item {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .status-item .label {
      font-weight: 500;
      color: #666;
      font-size: 0.9em;
    }

    .order-info {
      display: flex;
      flex-direction: column;
      gap: 12px;
      margin-top: 20px;
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

    .value {
      color: #333;
    }

    .value.amount {
      font-weight: 600;
      color: #2e7d32;
      font-size: 1.2em;
    }

    .order-item {
      display: flex;
      align-items: center;
      padding: 16px 0;
      gap: 16px;
    }

    .item-image {
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
      flex: 1;
      min-width: 0;
    }

    .item-details h3 {
      margin: 0 0 4px 0;
      font-size: 1.1em;
      color: #333;
    }

    .item-details p {
      margin: 2px 0;
      color: #666;
      font-size: 0.9em;
    }

    .item-quantity, .item-total {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 4px;
      min-width: 80px;
    }

    .quantity-label, .total-label {
      font-size: 0.8em;
      color: #666;
      text-transform: uppercase;
    }

    .quantity-value {
      font-weight: 600;
      color: #333;
    }

    .total-value {
      font-weight: 600;
      color: #2e7d32;
    }

    .order-summary {
      margin-top: 20px;
      padding-top: 16px;
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
      border-top: 2px solid #e0e0e0;
      margin-top: 8px;
      padding-top: 16px;
    }

    @media (max-width: 768px) {
      .order-detail-container {
        padding: 16px;
      }

      .status-section {
        flex-direction: column;
        gap: 12px;
      }

      .info-row {
        flex-direction: column;
        align-items: flex-start;
        gap: 4px;
      }

      .label {
        min-width: auto;
      }

      .order-item {
        flex-wrap: wrap;
        gap: 12px;
      }

      .item-quantity, .item-total {
        flex-direction: row;
        min-width: auto;
      }
    }
  `]
})
export class OrderDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private orderService = inject(OrderService);
  private snackBar = inject(MatSnackBar);

  order: Order | null = null;
  isLoading = true;
  OrderStatus = OrderStatus;

  ngOnInit(): void {
    const orderId = this.route.snapshot.paramMap.get('id');
    if (orderId) {
      this.loadOrder(+orderId);
    } else {
      this.isLoading = false;
    }
  }

  loadOrder(orderId: number): void {
    this.isLoading = true;
    this.orderService.getOrderById(orderId).subscribe({
      next: (order) => {
        this.order = order;
        this.isLoading = false;
      },
      error: (error: any) => {
        this.isLoading = false;
        console.error('Error loading order:', error);
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/orders']);
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

  downloadInvoice(): void {
    this.snackBar.open('Invoice download functionality coming soon!', 'Close', { duration: 3000 });
  }

  cancelOrder(): void {
    this.snackBar.open('Order cancellation functionality coming soon!', 'Close', { duration: 3000 });
  }

  reorder(): void {
    this.snackBar.open('Reorder functionality coming soon!', 'Close', { duration: 3000 });
  }

  trackOrder(): void {
    this.snackBar.open('Order tracking functionality coming soon!', 'Close', { duration: 3000 });
  }

  getSubtotal(): number {
    if (!this.order?.items) return 0;
    return this.order.items.reduce((sum, item) => sum + (item.quantity * item.unitPrice), 0);
  }

  getTax(): number {
    return this.getSubtotal() * 0.08; // 8% tax
  }

  getProductImage(item: any): string {
    // Use the imageUrl from the order item if available, otherwise use placeholder
    if (item.imageUrl) {
      return item.imageUrl;
    }
    const productName = item.productName || `Product ${item.productId}`;
    return `https://via.placeholder.com/80x80/e3f2fd/1976d2?text=${encodeURIComponent(productName)}`;
  }

  onImageError(event: any): void {
    event.target.src = 'https://via.placeholder.com/80x80/f5f5f5/999?text=No+Image';
  }
}