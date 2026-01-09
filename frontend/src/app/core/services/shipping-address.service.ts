import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ShippingAddress {
  shippingAddressId?: number;
  customerId: number;
  fullName: string;
  addressLine1: string;
  addressLine2?: string;
  city: string;
  state: string;
  postalCode: string;
  countryCode: string;
  phoneNumber?: string;
  isDefault?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ShippingAddressService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/shipping-addresses`;

  createShippingAddress(address: Omit<ShippingAddress, 'shippingAddressId'>): Observable<ShippingAddress> {
    return this.http.post<ShippingAddress>(this.apiUrl, address);
  }

  getMyShippingAddresses(): Observable<ShippingAddress[]> {
    return this.http.get<ShippingAddress[]>(`${this.apiUrl}/my-addresses`);
  }

  getShippingAddressById(id: number): Observable<ShippingAddress> {
    return this.http.get<ShippingAddress>(`${this.apiUrl}/${id}`);
  }
}