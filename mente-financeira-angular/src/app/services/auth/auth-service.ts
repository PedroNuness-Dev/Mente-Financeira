import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { LoginRequest } from '../../interfaces/auth/LoginRequest';
import { LoginResponse } from '../../interfaces/auth/LoginResponse';
import { tap } from 'rxjs';

const TOKEN_KEY = 'token';

@Injectable({
  providedIn: 'root',
})
export class AuthService {

  private httpClient = inject(HttpClient);
  private url = 'http://localhost:8080'

  login(loginRequest : LoginRequest){
    return this.httpClient.post<LoginResponse>(`${this.url}/api/auth/login`, loginRequest)
    .pipe(tap(res => localStorage.setItem(TOKEN_KEY, res.token)));
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  logout() {
    localStorage.removeItem(TOKEN_KEY);
  }
}
