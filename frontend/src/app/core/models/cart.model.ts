export interface CartItem {
  customerId: number;
  productId: number;
  productName: string;
  imageUrl?: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  addedAt: string;
  updatedAt: string;
}

export interface Cart {
  customerId: number;
  items: CartItem[];
  totalItems: number;
  totalAmount: number;
  lastUpdated: string;
}

export interface AddToCartRequest {
  productId: number;
  quantity: number;
}

export interface UpdateCartItemRequest {
  quantity: number;
}