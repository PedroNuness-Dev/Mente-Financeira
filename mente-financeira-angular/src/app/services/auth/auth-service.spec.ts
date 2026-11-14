import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { AuthService } from './auth-service';
import { UsuarioService } from '../usuario/usuario-service';

describe('AuthService', () => {
  let service: AuthService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
  });

  afterEach(() => localStorage.clear());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('logout apaga o token e o perfil do usuário da memória', () => {
    const usuarioService = TestBed.inject(UsuarioService);
    const httpMock = TestBed.inject(HttpTestingController);
    localStorage.setItem('token', 'jwt');
    localStorage.setItem('expire_at', '2999-01-01T00:00:00Z');
    usuarioService.buscarPerfilDoUsuarioAutenticado().subscribe();
    httpMock.expectOne('http://localhost:8080/api/users/me').flush({ id: 1, name: 'Pedro' });
    expect(usuarioService.usuario()).not.toBeNull();

    service.logout();

    expect(localStorage.getItem('token')).toBeNull();
    expect(service.isLoggedIn()).toBe(false);
    expect(usuarioService.usuario()).toBeNull();
  });
});
