import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { LoginRequest } from '../../interfaces/auth/LoginRequest';
import { LoginResponse } from '../../interfaces/auth/LoginResponse';
import { tap } from 'rxjs';
import { UsuarioService } from '../usuario/usuario-service';

const TOKEN_KEY = 'token';
const EXPIRE_AT = 'expire_at';

@Injectable({
  providedIn: 'root',
})
export class AuthService {

  private httpClient = inject(HttpClient);
  private usuarioService = inject(UsuarioService);
  private url = 'http://localhost:8080'

  login(loginRequest : LoginRequest){
    return this.httpClient.post<LoginResponse>(`${this.url}/api/auth/login`, loginRequest)
    .pipe(
      tap(res => {
        localStorage.setItem(TOKEN_KEY, res.token);
        localStorage.setItem(EXPIRE_AT, res.expiresAt);
      })
    )
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  isLoggedIn(): boolean {
    return !!this.getToken() && !this.isTokenExpirado();
  }

  logout() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(EXPIRE_AT);
    // Não deixa dados pessoais do usuário anterior na memória da aplicação
    this.usuarioService.limpar();
  }

  isTokenExpirado(){

    const expire_at = localStorage.getItem(EXPIRE_AT);

    if (!expire_at) {
      return true;
    }

    const expire_at_converted = new Date(expire_at);

    if (new Date() > expire_at_converted){
      return true;
    }

    return false;
  }
}
