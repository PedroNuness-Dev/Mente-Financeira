import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { UsuarioService } from './usuario-service';
import { UsuarioRequest } from '../../interfaces/usuario/UsuarioRequest';
import { UsuarioResponse } from '../../interfaces/usuario/UsuarioResponse';

describe('UsuarioService', () => {
  let service: UsuarioService;
  let httpMock: HttpTestingController;

  const usuario: UsuarioResponse = {
    id: 1,
    name: 'Pedro',
    email: 'pedro@teste.com',
    creationDate: '2026-11-14',
    updateDate: '2026-11-14',
    firstLogin: false,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(UsuarioService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('cadastrarUsuario deve fazer POST em /api/users com o corpo da requisição', () => {
    const request: UsuarioRequest = { name: 'Pedro', email: 'pedro@teste.com', password: 'Senha123!' };

    service.cadastrarUsuario(request).subscribe((res) => expect(res).toEqual(usuario));

    const req = httpMock.expectOne('http://localhost:8080/api/users');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(usuario);
  });

  it('buscarPerfilDoUsuarioAutenticado deve fazer GET em /api/users/me e guardar o usuário no signal', () => {
    expect(service.usuario()).toBeNull();

    service.buscarPerfilDoUsuarioAutenticado().subscribe();

    const req = httpMock.expectOne('http://localhost:8080/api/users/me');
    expect(req.request.method).toBe('GET');
    req.flush(usuario);

    expect(service.usuario()).toEqual(usuario);
  });
});
