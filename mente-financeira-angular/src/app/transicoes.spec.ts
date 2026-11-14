import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, ViewTransitionInfo } from '@angular/router';

import { aoCriarTransicao, CLASSE_ENTRAR_APP, CLASSE_SAIR_APP } from './transicoes';

describe('aoCriarTransicao', () => {
  const rota = (path: string) => ({ firstChild: { routeConfig: { path } } }) as unknown as ActivatedRouteSnapshot;

  let finalizar: () => void;

  const transicao = (de: string, para: string): ViewTransitionInfo => {
    const finished = new Promise<undefined>((resolve) => (finalizar = () => resolve(undefined)));
    return { transition: { finished } as ViewTransition, from: rota(de), to: rota(para) };
  };

  const classes = () => document.documentElement.classList;

  afterEach(() => classes().remove(CLASSE_ENTRAR_APP, CLASSE_SAIR_APP));

  it('ao sair do login para o app, aplica a classe de entrada e remove ao terminar', async () => {
    TestBed.runInInjectionContext(() => aoCriarTransicao(transicao('auth', '')));
    expect(classes().contains(CLASSE_ENTRAR_APP)).toBe(true);

    finalizar();
    await Promise.resolve();
    await Promise.resolve();

    expect(classes().contains(CLASSE_ENTRAR_APP)).toBe(false);
  });

  it('no logout (app -> login), aplica a classe de saída', () => {
    TestBed.runInInjectionContext(() => aoCriarTransicao(transicao('', 'auth')));
    expect(classes().contains(CLASSE_SAIR_APP)).toBe(true);
  });

  it('entre telas da mesma área, não aplica nenhuma classe', () => {
    TestBed.runInInjectionContext(() => aoCriarTransicao(transicao('auth', 'auth')));
    expect(classes().contains(CLASSE_ENTRAR_APP)).toBe(false);
    expect(classes().contains(CLASSE_SAIR_APP)).toBe(false);
  });
});
