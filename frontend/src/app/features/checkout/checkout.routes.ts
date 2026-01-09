import { Routes } from '@angular/router';
import { authGuard } from '../../core/guards/auth.guard';

export const checkoutRoutes: Routes = [
  {
    path: 'shipping',
    loadComponent: () => import('./shipping-address/shipping-address.component').then(m => m.ShippingAddressComponent),
    canActivate: [authGuard]
  },
  {
    path: 'payment',
    loadComponent: () => import('./payment/payment.component').then(m => m.PaymentComponent),
    canActivate: [authGuard]
  }
];