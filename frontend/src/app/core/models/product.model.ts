export interface Product {
  productId: number;
  name: string;
  description?: string;
  price: number;
  stockQty: number;
  imageUrl?: string;
  createdAt: string;
  inStock: boolean;
  totalSold?: number;
  totalRevenue?: number;
}

export interface ProductCreateRequest {
  name: string;
  description?: string;
  price: number;
  stockQty: number;
  imageUrl?: string;
}

export interface ProductUpdateRequest {
  name?: string;
  description?: string;
  price?: number;
  stockQty?: number;
  imageUrl?: string;
}

export interface ProductSearchRequest {
  name?: string;
  minPrice?: number;
  maxPrice?: number;
  inStockOnly?: boolean;
  sortBy?: string;
  sortDirection?: string;
}

export interface StockUpdateRequest {
  quantity: number;
  operation: 'increase' | 'decrease';
}