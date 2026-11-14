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
    return this.httpClient.post<UsuarioResponse>(`${this.url}/api/users`, usuarioRequest);
  }

  /** Remove o perfil da memória (usado no logout). */
  limpar(): void {
    this.usuarioAtual.set(null);
  }

  buscarPerfilDoUsuarioAutenticado() {
    return this.httpClient
      .get<UsuarioResponse>(`${this.url}/api/users/me`)
      .pipe(tap((res) => this.usuarioAtual.set(res)));
  }
}