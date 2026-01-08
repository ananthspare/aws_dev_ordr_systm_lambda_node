import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Product, ProductCreateRequest, ProductUpdateRequest, ProductSearchRequest } from '../models/product.model';
import { PagedResponse } from '../models/api-response.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/products`;

  getProducts(page: number = 0, size: number = 20, sortBy: string = 'createdAt', sortDir: string = 'desc'): Observable<PagedResponse<Product>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);

    return this.http.get<PagedResponse<Product>>(this.apiUrl, { params });
  }

  // Alias for getAllProducts to match component usage
  getAllProducts(): Observable<Product[]> {
    return new Observable(observer => {
      this.getProducts(0, 100).subscribe({
        next: (response) => {
          observer.next(response.content);
          observer.complete();
        },
        error: (error) => observer.error(error)
      });
    });
  }

  getProductById(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.apiUrl}/${id}`);
  }

  searchProducts(searchTerm: string, page: number = 0, size: number = 20): Observable<PagedResponse<Product>> {
    const params = new HttpParams()
      .set('searchTerm', searchTerm)
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PagedResponse<Product>>(`${this.apiUrl}/search`, { params });
  }

  getProductsByPriceRange(minPrice: number, maxPrice: number, page: number = 0, size: number = 20): Observable<PagedResponse<Product>> {
    const params = new HttpParams()
      .set('minPrice', minPrice.toString())
      .set('maxPrice', maxPrice.toString())
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PagedResponse<Product>>(`${this.apiUrl}/price-range`, { params });
  }

  getInStockProducts(page: number = 0, size: number = 20): Observable<PagedResponse<Product>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PagedResponse<Product>>(`${this.apiUrl}/in-stock`, { params });
  }

  getTopSellingProducts(page: number = 0, size: number = 10): Observable<PagedResponse<Product>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PagedResponse<Product>>(`${this.apiUrl}/top-selling`, { params });
  }

  createProduct(product: ProductCreateRequest): Observable<Product> {
    return this.http.post<Product>(this.apiUrl, product);
  }

  updateProduct(id: number, product: ProductUpdateRequest): Observable<Product> {
    return this.http.put<Product>(`${this.apiUrl}/${id}`, product);
  }

  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  checkStock(productId: number, quantity: number): Observable<boolean> {
    const params = new HttpParams().set('quantity', quantity.toString());
    return this.http.get<boolean>(`${this.apiUrl}/${productId}/stock/check`, { params });
  }
}