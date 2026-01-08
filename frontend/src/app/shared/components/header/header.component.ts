import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatBadgeModule } from '@angular/material/badge';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { AuthService } from '../../../core/services/auth.service';
import { CartService } from '../../../core/services/cart.service';
import { Router } from '@angular/router';
import { map } from 'rxjs/operators';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatBadgeModule,
    MatMenuModule,
    MatDividerModule
  ],
  template: `
    <mat-toolbar color="primary" class="header-toolbar">
      <div class="container">
        <div class="header-content">
          <!-- Logo/Brand -->
          <a routerLink="/" class="brand">
            <mat-icon>shopping_cart</mat-icon>
            <span>E-Commerce</span>
          </a>

          <!-- Navigation -->
          <nav class="nav-links">
            <a routerLink="/products" routerLinkActive="active" mat-button>
              Products
            </a>
            <a routerLink="/orders" routerLinkActive="active" mat-button *ngIf="isLoggedIn$ | async">
              Orders
            </a>
          </nav>

          <!-- User Actions -->
          <div class="user-actions">
            <!-- Cart -->
            <button mat-icon-button routerLink="/cart" [matBadge]="cartItemCount$ | async" 
                    matBadgeColor="accent" [matBadgeHidden]="(cartItemCount$ | async) === 0">
              <mat-icon>shopping_cart</mat-icon>
            </button>

            <!-- User Menu -->
            <div *ngIf="isLoggedIn$ | async; else loginButton">
              <button mat-icon-button [matMenuTriggerFor]="userMenu">
                <mat-icon>account_circle</mat-icon>
              </button>
              <mat-menu #userMenu="matMenu">
                <div class="user-info">
                  <span>{{ (currentUser$ | async)?.name }}</span>
                  <small>{{ (currentUser$ | async)?.email }}</small>
                </div>
                <mat-divider></mat-divider>
                <button mat-menu-item routerLink="/profile">
                  <mat-icon>person</mat-icon>
                  Profile
                </button>
                <button mat-menu-item routerLink="/orders">
                  <mat-icon>receipt</mat-icon>
                  My Orders
                </button>
                <mat-divider></mat-divider>
                <button mat-menu-item (click)="logout()">
                  <mat-icon>logout</mat-icon>
                  Logout
                </button>
              </mat-menu>
            </div>

            <ng-template #loginButton>
              <button mat-button routerLink="/auth/login">Login</button>
              <button mat-raised-button color="accent" routerLink="/auth/register">Register</button>
            </ng-template>
          </div>
        </div>
      </div>
    </mat-toolbar>
  `,
  styles: [`
    .header-toolbar {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      z-index: 1000;
    }

    .header-content {
      display: flex;
      align-items: center;
      justify-content: space-between;
      width: 100%;
    }

    .brand {
      display: flex;
      align-items: center;
      gap: 8px;
      text-decoration: none;
      color: inherit;
      font-size: 1.2em;
      font-weight: 500;
    }

    .nav-links {
      display: flex;
      gap: 8px;
    }

    .nav-links a.active {
      background-color: rgba(255, 255, 255, 0.1);
    }

    .user-actions {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .user-info {
      padding: 8px 16px;
      display: flex;
      flex-direction: column;
    }

    .user-info small {
      color: rgba(0, 0, 0, 0.6);
    }

    @media (max-width: 768px) {
      .nav-links {
        display: none;
      }
      
      .brand span {
        display: none;
      }
    }
  `]
})
export class HeaderComponent {
  private authService = inject(AuthService);
  private cartService = inject(CartService);
  private router = inject(Router);

  currentUser$ = this.authService.currentUser$;
  isLoggedIn$ = this.authService.isLoggedIn$;
  cartItemCount$ = this.cartService.cart$.pipe(
    map((cart: any) => cart.totalItems)
  );

  logout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.router.navigate(['/products']);
      },
      error: (error: any) => {
        console.error('Logout error:', error);
        // Force logout even if API call fails
        this.router.navigate(['/products']);
      }
    });
  }
}