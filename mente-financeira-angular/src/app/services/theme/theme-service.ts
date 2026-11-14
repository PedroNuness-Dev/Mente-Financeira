import { DOCUMENT } from '@angular/common';
import { inject, Injectable, signal } from '@angular/core';

export type Tema = 'claro' | 'escuro';

export const TEMA_KEY = 'tema';

/** Ponto da tela (em px) de onde parte a animação de troca de tema. */
export interface OrigemTransicao {
  x: number;
  y: number;
}

/**
 * Fonte única do tema da aplicação.
 * Aplica `data-tema` no <html>, para que os tokens globais valham em todas as telas,
 * e persiste a escolha no localStorage.
 */
@Injectable({
  providedIn: 'root',
})
export class ThemeService {
  private readonly document = inject(DOCUMENT);
  private readonly temaAtual = signal<Tema>(this.lerTemaInicial());

  readonly tema = this.temaAtual.asReadonly();

  constructor() {
    this.aplicar(this.temaAtual());
  }

  /**
   * Alterna entre claro e escuro. Com `origem`, usa a View Transitions API para
   * revelar o novo tema num círculo que cresce a partir desse ponto.
   */
  alternar(origem?: OrigemTransicao): void {
    const novoTema: Tema = this.temaAtual() === 'claro' ? 'escuro' : 'claro';
    const raiz = this.document.documentElement;
    const view = this.document.defaultView;

    const podeAnimar =
      !!origem &&
      typeof this.document.startViewTransition === 'function' &&
      !view?.matchMedia?.('(prefers-reduced-motion: reduce)').matches;

    if (!podeAnimar || !origem || !view) {
      this.definir(novoTema);
      return;
    }

    const raio = Math.hypot(
      Math.max(origem.x, view.innerWidth - origem.x),
      Math.max(origem.y, view.innerHeight - origem.y),
    );
    raiz.style.setProperty('--vt-x', `${origem.x}px`);
    raiz.style.setProperty('--vt-y', `${origem.y}px`);
    raiz.style.setProperty('--vt-raio', `${raio}px`);
    raiz.classList.add('vt-tema');

    const transicao = this.document.startViewTransition(() => this.definir(novoTema));
    transicao.finished.finally(() => raiz.classList.remove('vt-tema'));
  }

  definir(tema: Tema): void {
    this.temaAtual.set(tema);
    this.aplicar(tema);
    try {
      localStorage.setItem(TEMA_KEY, tema);
    } catch {
      // Armazenamento indisponível (modo privado etc.): o tema vale só para esta sessão.
    }
  }

  private aplicar(tema: Tema): void {
    this.document.documentElement.setAttribute('data-tema', tema);
  }

  private lerTemaInicial(): Tema {
    try {
      const salvo = localStorage.getItem(TEMA_KEY);
      if (salvo === 'claro' || salvo === 'escuro') return salvo;
    } catch {
      // Sem acesso ao armazenamento: cai na preferência do sistema.
    }
    const prefereEscuro = this.document.defaultView?.matchMedia?.('(prefers-color-scheme: dark)').matches;
    return prefereEscuro ? 'escuro' : 'claro';
  }
}
