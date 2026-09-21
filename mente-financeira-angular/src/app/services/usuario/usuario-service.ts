import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { UsuarioRequest } from '../../interfaces/usuario/UsuarioRequest';
import { UsuarioResponse } from '../../interfaces/usuario/UsuarioResponse';

@Injectable({
  providedIn: 'root',
})
export class UsuarioService {

  private httpClient = inject(HttpClient);
  private url = 'http://localhost:8080'

  cadastrarUsuario(usuarioRequest : UsuarioRequest){
    return this.httpClient.post<UsuarioResponse>(`${this.url}/api/usuarios/cadastro`, usuarioRequest);
  }
}
