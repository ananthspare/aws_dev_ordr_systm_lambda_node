import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface CheckoutRequest {
  customerId: number;
  countryCode: string;
  orderItems: Array<{
    productId: number;
    quantity: number;
  }>;
  paymentMethod: string;
  currency: string;
}

export interface CheckoutResponse {
  order: {
    orderId: number;
    totalAmount: number;
    status: string;
    paymentStatus: string;
  };
  payment: {
    paymentId: number;
    status: string;
    transactionRef: string;
  };
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class CheckoutService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/orders`;

  checkout(request: CheckoutRequest): Observable<CheckoutResponse> {
    return this.http.post<CheckoutResponse>(`${this.apiUrl}/checkout`, request);
  }
}