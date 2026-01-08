import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Order, OrderCreateRequest, OrderStatus, PaymentStatus } from '../models/order.model';
import { PagedResponse } from '../models/api-response.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/orders`;

  createOrder(orderRequest: OrderCreateRequest): Observable<Order> {
    return this.http.post<Order>(this.apiUrl, orderRequest);
  }

  getOrderById(id: number): Observable<Order> {
    return this.http.get<Order>(`${this.apiUrl}/${id}`);
  }

  getOrders(page: number = 0, size: number = 20): Observable<PagedResponse<Order>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PagedResponse<Order>>(this.apiUrl, { params });
  }

  getOrdersByCustomer(customerId: number, page: number = 0, size: number = 20): Observable<PagedResponse<Order>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PagedResponse<Order>>(`${this.apiUrl}/customer/${customerId}`, { params });
  }

  // Alias for getCustomerOrders to match component usage
  getCustomerOrders(): Observable<Order[]> {
    return new Observable(observer => {
      // For now, return empty array. In real implementation, get current user ID
      observer.next([]);
      observer.complete();
    });
  }

  getOrdersByStatus(status: OrderStatus, page: number = 0, size: number = 20): Observable<PagedResponse<Order>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PagedResponse<Order>>(`${this.apiUrl}/status/${status}`, { params });
  }

  getRecentOrdersByCustomer(customerId: number, limit: number = 5): Observable<Order[]> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<Order[]>(`${this.apiUrl}/customer/${customerId}/recent`, { params });
  }

  updateOrderStatus(orderId: number, status: OrderStatus): Observable<Order> {
    return this.http.put<Order>(`${this.apiUrl}/${orderId}/status`, { status });
  }

  updatePaymentStatus(orderId: number, paymentStatus: PaymentStatus): Observable<Order> {
    return this.http.put<Order>(`${this.apiUrl}/${orderId}/payment-status`, { paymentStatus });
  }

  cancelOrder(orderId: number): Observable<Order> {
    return this.http.put<Order>(`${this.apiUrl}/${orderId}/cancel`, {});
  }

  searchOrdersByCustomer(searchTerm: string, page: number = 0, size: number = 20): Observable<PagedResponse<Order>> {
    const params = new HttpParams()
      .set('searchTerm', searchTerm)
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PagedResponse<Order>>(`${this.apiUrl}/search`, { params });
  }
}