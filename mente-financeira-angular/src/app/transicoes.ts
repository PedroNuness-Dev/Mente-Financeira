import { DOCUMENT } from '@angular/common';
import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, ViewTransitionInfo } from '@angular/router';
import { SplashService } from './services/splash/splash-service';

type Area = 'auth' | 'app';

/** Classes aplicadas no <html> durante a View Transition (animações em src/styles/_motion.scss). */
export const CLASSE_ENTRAR_APP = 'vt-entrar-app';
export const CLASSE_SAIR_APP = 'vt-sair-app';

function area(rota: ActivatedRouteSnapshot): Area {
  return rota.firstChild?.routeConfig?.path === 'auth' ? 'auth' : 'app';
}

/**
 * Chamado pelo router a cada View Transition. Quando a navegação cruza a fronteira
 * entre as telas de autenticação e o app, marca o <html> com uma classe para que
 * o CSS use uma animação de "entrada no app" (ou de saída, no logout).
 * Roda antes de o navegador capturar a tela antiga, então a classe já vale para ela.
 */
export function aoCriarTransicao({ transition, from, to }: ViewTransitionInfo): void {
  // A splash de boas-vindas já cobre a tela e cuida da animação: não sobrepõe outra
  if (inject(SplashService).ativa()) {
    transition.skipTransition();
    return;
  }

  const de = area(from);
  const para = area(to);
  if (de === para) return;

  const classe = para === 'app' ? CLASSE_ENTRAR_APP : CLASSE_SAIR_APP;
  const raiz = inject(DOCUMENT).documentElement;

  raiz.classList.add(classe);
  transition.finished.finally(() => raiz.classList.remove(classe));
}
