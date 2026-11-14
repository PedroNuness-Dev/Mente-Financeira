import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from './services/auth/auth-service';

/** Rotas do app: só com sessão válida; senão, redireciona para o login. */
export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Retornar UrlTree (em vez de navigate + false) deixa o router tratar o
  // redirecionamento como parte da mesma navegação, sem navegações concorrentes.
  return authService.isLoggedIn() ? true : router.createUrlTree(['/auth/login']);
};

/**
 * Rotas de autenticação (login/cadastro): só para visitantes.
 * Quem já tem sessão válida — por exemplo, ao clicar em "voltar" estando na home —
 * é mandado de volta para o app em vez de ver o formulário de login.
 */
export const guestGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.isLoggedIn() ? router.createUrlTree(['/home']) : true;
};
