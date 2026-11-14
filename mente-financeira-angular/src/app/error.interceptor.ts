import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './services/auth/auth-service';

/** No login, 401 significa credenciais erradas — quem trata é a própria tela. */
const ROTA_LOGIN = '/api/auth/login';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error) => {
      if (error.status === 401 && !req.url.endsWith(ROTA_LOGIN)) {
        authService.logout();
        router.navigate(['/auth/login'], { replaceUrl: true });
      }

      return throwError(() => error);
    })
  );
};
