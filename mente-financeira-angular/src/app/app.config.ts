import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter, withViewTransitions } from '@angular/router';

import { routes } from './app.routes';
import { authInterceptor } from './auth.interceptor';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { errorInterceptor } from './error.interceptor';
import { aoCriarTransicao } from './transicoes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    // View Transitions: troca de rota suave (animações definidas em src/styles/_motion.scss)
    provideRouter(
      routes,
      withViewTransitions({ skipInitialTransition: true, onViewTransitionCreated: aoCriarTransicao }),
    ),
    provideHttpClient(withInterceptors([authInterceptor, errorInterceptor])),
  ]
};
