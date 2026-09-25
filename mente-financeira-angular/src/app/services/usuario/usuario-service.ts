import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { tap } from 'rxjs';
import { UsuarioRequest } from '../../interfaces/usuario/UsuarioRequest';
import { UsuarioResponse } from '../../interfaces/usuario/UsuarioResponse';

@Injectable({
  providedIn: 'root',
})
export class UsuarioService {
  
  private httpClient = inject(HttpClient);
  private url = 'http://localhost:8080';

  private usuarioAtual = signal<UsuarioResponse | null>(null);
  readonly usuario = this.usuarioAtual.asReadonly();

  cadastrarUsuario(usuarioRequest: UsuarioRequest) {
    return this.httpClient.post<UsuarioResponse>(`${this.url}/api/users/register`, usuarioRequest);
  }

  buscarPerfilDoUsuarioAutenticado() {
    return this.httpClient
      .get<UsuarioResponse>(`${this.url}/api/users/profile`)
      .pipe(tap((res) => this.usuarioAtual.set(res)));
  }
}