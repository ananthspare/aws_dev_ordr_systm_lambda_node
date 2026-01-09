export interface Order {
  orderId: number;
  customerId: number;
  customerName: string;
  customerEmail: string;
  status: OrderStatus;
  countryCode: string;
  totalAmount: number;
  invoiceS3Path?: string;
  paymentStatus: PaymentStatus;
  createdAt: string;
  totalItems: number;
  orderItems: OrderItem[];
  items: OrderItem[]; // Alias for orderItems for component compatibility
  shippingAddress?: ShippingAddress;
}

export interface ShippingAddress {
  shippingAddressId: number;
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

export interface OrderItem {
  orderItemId: number;
  orderId: number;
  productId: number;
  productName: string;
  productDescription?: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  imageUrl?: string;
}

export interface OrderCreateRequest {
  customerId: number;
  countryCode: string;
  orderItems: OrderItemCreateRequest[];
}

export interface OrderItemCreateRequest {
  productId: number;
  quantity: number;
}

export enum OrderStatus {
  CREATED = 'CREATED',
  PENDING = 'PENDING',
  CONFIRMED = 'CONFIRMED',
  PROCESSING = 'PROCESSING',
  SHIPPED = 'SHIPPED',
  DELIVERED = 'DELIVERED',
  CANCELLED = 'CANCELLED',
  REFUNDED = 'REFUNDED'
}

export enum PaymentStatus {
  PENDING = 'PENDING',
  PAID = 'PAID',
  FAILED = 'FAILED',
  REFUNDED = 'REFUNDED'
}