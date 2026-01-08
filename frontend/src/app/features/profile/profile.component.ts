import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../core/services/auth.service';
import { Customer } from '../../core/models/customer.model';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  template: `
    <div class="profile-container">
      <div class="header">
        <h1>My Profile</h1>
        <button mat-button routerLink="/products">
          <mat-icon>arrow_back</mat-icon>
          Back to Shopping
        </button>
      </div>

      <div *ngIf="isLoading" class="loading-container">
        <mat-spinner></mat-spinner>
        <p>Loading profile...</p>
      </div>

      <div *ngIf="!isLoading && customer" class="profile-content">
        <mat-card class="profile-card">
          <mat-card-header>
            <div mat-card-avatar class="profile-avatar">
              <mat-icon>person</mat-icon>
            </div>
            <mat-card-title>{{ customer.name }}</mat-card-title>
            <mat-card-subtitle>{{ customer.email }}</mat-card-subtitle>
          </mat-card-header>

          <mat-card-content>
            <div class="profile-info">
              <div class="info-row">
                <span class="label">Customer ID:</span>
                <span class="value">{{ customer.customerId }}</span>
              </div>
              
              <div class="info-row">
                <span class="label">Name:</span>
                <span class="value">{{ customer.name }}</span>
              </div>
              
              <div class="info-row">
                <span class="label">Email:</span>
                <span class="value">{{ customer.email }}</span>
              </div>
              
              <div class="info-row">
                <span class="label">Member Since:</span>
                <span class="value">{{ customer.createdAt | date:'mediumDate' }}</span>
              </div>
              
              <div class="info-row" *ngIf="customer.totalOrders">
                <span class="label">Total Orders:</span>
                <span class="value">{{ customer.totalOrders }}</span>
              </div>
              
              <div class="info-row" *ngIf="customer.lastOrderDate">
                <span class="label">Last Order:</span>
                <span class="value">{{ customer.lastOrderDate | date:'mediumDate' }}</span>
              </div>
            </div>
          </mat-card-content>

          <mat-card-actions>
            <button mat-button color="primary" (click)="editProfile()">
              <mat-icon>edit</mat-icon>
              Edit Profile
            </button>
            
            <button mat-button (click)="changePassword()">
              <mat-icon>lock</mat-icon>
              Change Password
            </button>
            
            <button mat-button routerLink="/orders">
              <mat-icon>receipt</mat-icon>
              View Orders
            </button>
          </mat-card-actions>
        </mat-card>

        <mat-card class="quick-actions">
          <mat-card-header>
            <mat-card-title>Quick Actions</mat-card-title>
          </mat-card-header>
          
          <mat-card-content>
            <div class="actions-grid">
              <button mat-raised-button color="primary" routerLink="/products">
                <mat-icon>shopping_bag</mat-icon>
                <span>Shop Products</span>
              </button>
              
              <button mat-raised-button routerLink="/cart">
                <mat-icon>shopping_cart</mat-icon>
                <span>View Cart</span>
              </button>
              
              <button mat-raised-button routerLink="/orders">
                <mat-icon>receipt_long</mat-icon>
                <span>Order History</span>
              </button>
              
              <button mat-raised-button (click)="logout()">
                <mat-icon>logout</mat-icon>
                <span>Logout</span>
              </button>
            </div>
          </mat-card-content>
        </mat-card>
      </div>
    </div>
  `,
  styles: [`
    .profile-container {
      padding: 20px;
      max-width: 800px;
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

    .profile-content {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .profile-card {
      margin-bottom: 20px;
    }

    .profile-avatar {
      background-color: #3f51b5;
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .profile-info {
      display: flex;
      flex-direction: column;
      gap: 16px;
      margin-top: 16px;
    }

    .info-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 0;
      border-bottom: 1px solid #f0f0f0;
    }

    .info-row:last-child {
      border-bottom: none;
    }

    .label {
      font-weight: 500;
      color: #666;
      min-width: 140px;
    }

    .value {
      color: #333;
      text-align: right;
    }

    .actions-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
      gap: 16px;
    }

    .actions-grid button {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;
      padding: 16px;
      height: auto;
    }

    .actions-grid button mat-icon {
      font-size: 24px;
      width: 24px;
      height: 24px;
    }

    mat-card-actions {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }

    @media (max-width: 768px) {
      .profile-container {
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

      .value {
        text-align: left;
      }

      .actions-grid {
        grid-template-columns: repeat(2, 1fr);
      }

      mat-card-actions {
        flex-direction: column;
      }
    }
  `]
})
export class ProfileComponent implements OnInit {
  private authService = inject(AuthService);
  private snackBar = inject(MatSnackBar);

  customer: Customer | null = null;
  isLoading = true;

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.isLoading = true;
    // Get current user from auth service
    this.customer = this.authService.getCurrentUser();
    this.isLoading = false;
  }

  editProfile(): void {
    this.snackBar.open('Profile editing functionality coming soon!', 'Close', { duration: 3000 });
  }

  changePassword(): void {
    this.snackBar.open('Change password functionality coming soon!', 'Close', { duration: 3000 });
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.snackBar.open('Logged out successfully', 'Close', { duration: 2000 });
      },
      error: (error: any) => {
        console.error('Logout error:', error);
        this.snackBar.open('Logout failed', 'Close', { duration: 3000 });
      }
    });
  }
}