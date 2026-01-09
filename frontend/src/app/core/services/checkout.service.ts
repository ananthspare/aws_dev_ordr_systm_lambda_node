import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { ShippingAddress } from './shipping-address.service';

@Injectable({
  providedIn: 'root'
})
export class CheckoutService {
  private shippingAddressSubject = new BehaviorSubject<ShippingAddress | null>(null);
  public shippingAddress$ = this.shippingAddressSubject.asObservable();

  setShippingAddress(address: ShippingAddress): void {
    this.shippingAddressSubject.next(address);
  }

  getShippingAddress(): ShippingAddress | null {
    return this.shippingAddressSubject.value;
  }

  getCustomerId(): number | null {
    // Get from localStorage where it's stored after login
    const customerData = localStorage.getItem('customer');
    if (customerData) {
      try {
        const customer = JSON.parse(customerData);
        return customer.customerId;
      } catch (e) {
        console.error('Error parsing customer data:', e);
      }
    }
    
    // Fallback: try to get from auth token or other storage
    const token = localStorage.getItem('token');
    if (token && token.startsWith('mock-jwt-token-')) {
      const customerId = token.replace('mock-jwt-token-', '');
      return parseInt(customerId);
    }
    
    return null;
  }

  clearCheckoutData(): void {
    this.shippingAddressSubject.next(null);
  }
}