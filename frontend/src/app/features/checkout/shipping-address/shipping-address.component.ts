import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ShippingAddressService } from '../../../core/services/shipping-address.service';
import { CheckoutService } from '../../../core/services/checkout.service';

@Component({
  selector: 'app-shipping-address',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatCheckboxModule,
    MatSnackBarModule,
    MatProgressSpinnerModule
  ],
  template: `
    <div class="shipping-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>Shipping Address</mat-card-title>
          <mat-card-subtitle>Enter your shipping information</mat-card-subtitle>
        </mat-card-header>

        <mat-card-content>
          <form [formGroup]="shippingForm" (ngSubmit)="onSubmit()">
            <div class="form-row">
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Full Name</mat-label>
                <input matInput formControlName="fullName" required>
                <mat-error *ngIf="shippingForm.get('fullName')?.hasError('required')">
                  Full name is required
                </mat-error>
              </mat-form-field>
            </div>

            <div class="form-row">
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Address Line 1</mat-label>
                <input matInput formControlName="addressLine1" required>
                <mat-error *ngIf="shippingForm.get('addressLine1')?.hasError('required')">
                  Address is required
                </mat-error>
              </mat-form-field>
            </div>

            <div class="form-row">
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Address Line 2 (Optional)</mat-label>
                <input matInput formControlName="addressLine2">
              </mat-form-field>
            </div>

            <div class="form-row">
              <mat-form-field appearance="outline" class="half-width">
                <mat-label>City</mat-label>
                <input matInput formControlName="city" required>
                <mat-error *ngIf="shippingForm.get('city')?.hasError('required')">
                  City is required
                </mat-error>
              </mat-form-field>

              <mat-form-field appearance="outline" class="half-width">
                <mat-label>State</mat-label>
                <input matInput formControlName="state" required>
                <mat-error *ngIf="shippingForm.get('state')?.hasError('required')">
                  State is required
                </mat-error>
              </mat-form-field>
            </div>

            <div class="form-row">
              <mat-form-field appearance="outline" class="half-width">
                <mat-label>Postal Code</mat-label>
                <input matInput formControlName="postalCode" required>
                <mat-error *ngIf="shippingForm.get('postalCode')?.hasError('required')">
                  Postal code is required
                </mat-error>
              </mat-form-field>

              <mat-form-field appearance="outline" class="half-width">
                <mat-label>Country</mat-label>
                <mat-select formControlName="countryCode" required>
                  <mat-option value="US">United States</mat-option>
                  <mat-option value="CA">Canada</mat-option>
                  <mat-option value="GB">United Kingdom</mat-option>
                </mat-select>
                <mat-error *ngIf="shippingForm.get('countryCode')?.hasError('required')">
                  Country is required
                </mat-error>
              </mat-form-field>
            </div>

            <div class="form-row">
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Phone Number (Optional)</mat-label>
                <input matInput formControlName="phoneNumber">
              </mat-form-field>
            </div>

            <div class="form-row">
              <mat-checkbox formControlName="isDefault">
                Set as default address
              </mat-checkbox>
            </div>
          </form>
        </mat-card-content>

        <mat-card-actions>
          <button mat-button type="button" (click)="goBack()">Back</button>
          <button mat-raised-button color="primary" 
                  (click)="onSubmit()" 
                  [disabled]="shippingForm.invalid || isLoading">
            <mat-spinner *ngIf="isLoading" diameter="20"></mat-spinner>
            Continue to Payment
          </button>
        </mat-card-actions>
      </mat-card>
    </div>
  `,
  styles: [`
    .shipping-container {
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

    .half-width {
      width: calc(50% - 8px);
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
export class ShippingAddressComponent implements OnInit {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);
  private shippingAddressService = inject(ShippingAddressService);
  private checkoutService = inject(CheckoutService);

  shippingForm!: FormGroup;
  isLoading = false;

  ngOnInit(): void {
    this.initForm();
  }

  private initForm(): void {
    this.shippingForm = this.fb.group({
      fullName: ['', [Validators.required, Validators.maxLength(150)]],
      addressLine1: ['', [Validators.required, Validators.maxLength(255)]],
      addressLine2: ['', Validators.maxLength(255)],
      city: ['', [Validators.required, Validators.maxLength(100)]],
      state: ['', [Validators.required, Validators.maxLength(100)]],
      postalCode: ['', [Validators.required, Validators.maxLength(20)]],
      countryCode: ['US', [Validators.required, Validators.minLength(2), Validators.maxLength(2)]],
      phoneNumber: ['', Validators.maxLength(20)],
      isDefault: [false]
    });
  }

  onSubmit(): void {
    if (this.shippingForm.valid) {
      this.isLoading = true;
      
      const customerId = this.checkoutService.getCustomerId();
      if (!customerId) {
        this.snackBar.open('Please login to continue', 'Close', { duration: 3000 });
        this.router.navigate(['/auth/login']);
        return;
      }

      const shippingData = {
        ...this.shippingForm.value,
        customerId: customerId
      };

      this.shippingAddressService.createShippingAddress(shippingData).subscribe({
        next: (address) => {
          this.checkoutService.setShippingAddress(address);
          this.router.navigate(['/checkout/payment']);
        },
        error: (error) => {
          this.isLoading = false;
          this.snackBar.open('Failed to save shipping address', 'Close', { duration: 3000 });
          console.error('Error saving shipping address:', error);
        }
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/cart']);
  }
}