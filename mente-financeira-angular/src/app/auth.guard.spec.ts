import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, provideRouter, RouterStateSnapshot, UrlTree } from '@angular/router';

import { authGuard, guestGuard } from './auth.guard';
import { AuthService } from './services/auth/auth-service';

describe('guards de autenticação', () => {
  let logado: boolean;

  const executar = (guard: typeof authGuard) =>
    TestBed.runInInjectionContext(() =>
      guard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
    );

  const destino = (resultado: unknown) => (resultado as UrlTree).toString();

  beforeEach(() => {
    logado = false;
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: { isLoggedIn: () => logado } },
      ],
    });
  });

  describe('authGuard', () => {
    it('libera o app com sessão válida', () => {
      logado = true;
      expect(executar(authGuard)).toBe(true);
    });

    it('sem sessão, redireciona para o login', () => {
      expect(destino(executar(authGuard))).toBe('/auth/login');
    });
  });

  describe('guestGuard', () => {
    it('libera login/cadastro para visitantes', () => {
      expect(executar(guestGuard)).toBe(true);
    });

    it('com sessão válida (ex.: "voltar" estando na home), manda de volta para o app', () => {
      logado = true;
      expect(destino(executar(guestGuard))).toBe('/home');
    });
  });
});
