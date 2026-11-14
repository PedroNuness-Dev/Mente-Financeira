import { TestBed } from '@angular/core/testing';

import {
  CLASSE_SPLASH_ATIVA,
  SplashService,
  TEMPO_COBRIR_MS,
  TEMPO_MAXIMO_MS,
  TEMPO_MINIMO_MS,
} from './splash-service';

describe('SplashService', () => {
  let service: SplashService;
  const config = { titulo: 'Bem-vindo(a)!', subtitulo: 'Preparando seu painel...' };
  const classeAtiva = () => document.documentElement.classList.contains(CLASSE_SPLASH_ATIVA);

  beforeEach(() => {
    vi.useFakeTimers();
    service = TestBed.inject(SplashService);
  });

  afterEach(() => {
    service.encerrar();
    vi.useRealTimers();
  });

  it('mostrar ativa a splash e só resolve quando a tela já está coberta', async () => {
    let cobriu = false;
    service.mostrar(config).then(() => (cobriu = true));

    expect(service.ativa()).toBe(true);
    expect(classeAtiva()).toBe(true);

    await vi.advanceTimersByTimeAsync(TEMPO_COBRIR_MS - 1);
    expect(cobriu).toBe(false);

    await vi.advanceTimersByTimeAsync(1);
    expect(cobriu).toBe(true);
  });

  it('liberada antes do tempo mínimo, espera o tempo mínimo para sair', async () => {
    service.mostrar(config);
    service.liberar();

    await vi.advanceTimersByTimeAsync(TEMPO_MINIMO_MS - 1);
    expect(service.ativa()).toBe(true);

    await vi.advanceTimersByTimeAsync(1);
    expect(service.ativa()).toBe(false);
    expect(classeAtiva()).toBe(false);
  });

  it('depois do tempo mínimo, sai assim que for liberada', async () => {
    service.mostrar(config);
    await vi.advanceTimersByTimeAsync(TEMPO_MINIMO_MS + 500);
    expect(service.ativa()).toBe(true);

    service.liberar();
    expect(service.ativa()).toBe(false);
  });

  it('nunca fica presa: sai sozinha no tempo máximo mesmo sem ser liberada', async () => {
    service.mostrar(config);

    await vi.advanceTimersByTimeAsync(TEMPO_MAXIMO_MS);
    expect(service.ativa()).toBe(false);
  });

  it('liberar sem splash ativa não faz nada (ex.: recarregar a página já logado)', () => {
    expect(() => service.liberar()).not.toThrow();
    expect(service.ativa()).toBe(false);
  });
});
