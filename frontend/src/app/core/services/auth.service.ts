import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Customer, LoginRequest, LoginResponse, CustomerCreateRequest } from '../models/customer.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private currentUserSubject = new BehaviorSubject<Customer | null>(null);
  private isLoggedInSubject = new BehaviorSubject<boolean>(false);

  public currentUser$ = this.currentUserSubject.asObservable();
  public isLoggedIn$ = this.isLoggedInSubject.asObservable();

  constructor() {
    this.loadUserFromStorage();
  }

  register(request: CustomerCreateRequest): Observable<Customer> {
    return this.http.post<Customer>(`${environment.apiUrl}/auth/register`, request);
  }

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, request)
      .pipe(
        tap(response => {
          if (response.token && response.customer) {
            this.setCurrentUser(response.customer, response.token);
          }
        })
      );
  }

  logout(): Observable<any> {
    return this.http.post(`${environment.apiUrl}/auth/logout`, {})
      .pipe(
        tap(() => {
          this.clearCurrentUser();
        })
      );
  }

  checkEmailAvailability(email: string): Observable<{ email: string; available: boolean; exists: boolean }> {
    return this.http.get<{ email: string; available: boolean; exists: boolean }>(
      `${environment.apiUrl}/auth/check-email?email=${email}`
    );
  }

  refreshToken(): Observable<{ message: string; token: string }> {
    return this.http.post<{ message: string; token: string }>(
      `${environment.apiUrl}/auth/refresh`, {}
    ).pipe(
      tap(response => {
        if (response.token) {
          localStorage.setItem('token', response.token);
        }
      })
    );
  }

  private setCurrentUser(user: Customer, token: string): void {
    localStorage.setItem('currentUser', JSON.stringify(user));
    localStorage.setItem('token', token);
    this.currentUserSubject.next(user);
    this.isLoggedInSubject.next(true);
  }

  private clearCurrentUser(): void {
    localStorage.removeItem('currentUser');
    localStorage.removeItem('token');
    this.currentUserSubject.next(null);
    this.isLoggedInSubject.next(false);
  }

  private loadUserFromStorage(): void {
    const userStr = localStorage.getItem('currentUser');
    const token = localStorage.getItem('token');
    
    if (userStr && token) {
      try {
        const user = JSON.parse(userStr);
        this.currentUserSubject.next(user);
        this.isLoggedInSubject.next(true);
      } catch (error) {
        this.clearCurrentUser();
      }
    }
  }

  getCurrentUser(): Customer | null {
    return this.currentUserSubject.value;
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  isAuthenticated(): boolean {
    return this.isLoggedInSubject.value && !!this.getToken();
  }
}