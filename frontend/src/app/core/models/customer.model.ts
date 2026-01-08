export interface Customer {
  customerId: number;
  name: string;
  email: string;
  createdAt: string;
  totalOrders?: number;
  lastOrderDate?: string;
}

export interface CustomerCreateRequest {
  name: string;
  email: string;
  password: string;
}

export interface CustomerUpdateRequest {
  name?: string;
  email?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  message: string;
  customer: Customer;
  token: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}