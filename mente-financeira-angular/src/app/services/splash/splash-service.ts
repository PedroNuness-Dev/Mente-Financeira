import { DOCUMENT } from '@angular/common';
import { computed, inject, Injectable, signal } from '@angular/core';

export interface SplashConfig {
  titulo: string;
  subtitulo: string;
  /** Ponto (px) de onde partem as ondas de cor — normalmente o botão clicado. */
  origem?: { x: number; y: number };
}

/** Classe no <html> enquanto a splash cobre a tela (pausa as animações de entrada do app). */
export const CLASSE_SPLASH_ATIVA = 'splash-ativa';

/** Tempo até as ondas de cor cobrirem a tela inteira: só então a rota troca por baixo. */
export const TEMPO_COBRIR_MS = 1000;
/** Tempo mínimo de exibição, para a mensagem ser lida. */
export const TEMPO_MINIMO_MS = 2200;
/** Rede de segurança: a splash nunca fica presa na tela. */
export const TEMPO_MAXIMO_MS = 8000;

/**
 * Controla a tela de transição "entrando no app".
 * Fluxo: mostrar() -> (ondas cobrem a tela) -> rota troca por baixo ->
 * liberar() quando o app carregou -> após o tempo mínimo, a splash sai.
 */
@Injectable({
  providedIn: 'root',
})
export class SplashService {
  private readonly document = inject(DOCUMENT);

  private readonly configAtual = signal<SplashConfig | null>(null);
  readonly config = this.configAtual.asReadonly();
  readonly ativa = computed(() => this.configAtual() !== null);

  /** Elemento do mascote na tela de origem (registrado pelo hero), para a animação de "voo". */
  private mascoteOrigem: HTMLElement | null = null;
  private retanguloMascote: DOMRect | null = null;

  private liberada = false;
  private tempoMinimoAtingido = false;
  private timers: ReturnType<typeof setTimeout>[] = [];

  registrarMascoteOrigem(elemento: HTMLElement | null): void {
    this.mascoteOrigem = elemento;
  }

  /** Posição do mascote de origem no momento em que a splash começou (null se não estava visível). */
  posicaoMascoteOrigem(): DOMRect | null {
    return this.retanguloMascote;
  }

  /** Exibe a splash. A promessa resolve quando a tela já está coberta (hora de trocar a rota). */
  mostrar(config: SplashConfig): Promise<void> {
    this.limparTimers();
    const retangulo = this.mascoteOrigem?.getBoundingClientRect() ?? null;
    this.retanguloMascote = retangulo && retangulo.width > 0 ? retangulo : null;
    this.liberada = false;
    this.tempoMinimoAtingido = false;

    this.configAtual.set(config);
    this.document.documentElement.classList.add(CLASSE_SPLASH_ATIVA);

    this.agendar(() => {
      this.tempoMinimoAtingido = true;
      this.tentarEncerrar();
    }, TEMPO_MINIMO_MS);
    this.agendar(() => this.encerrar(), TEMPO_MAXIMO_MS);

    return new Promise((resolve) => this.agendar(resolve, TEMPO_COBRIR_MS));
  }

  /** Avisa que o app terminou de carregar; a splash sai assim que o tempo mínimo passar. */
  liberar(): void {
    if (!this.ativa()) return;
    this.liberada = true;
    this.tentarEncerrar();
  }

  encerrar(): void {
    if (!this.ativa()) return;
    this.limparTimers();
    // Remove a classe primeiro: as animações de entrada do app tocam enquanto a cortina sobe
    this.document.documentElement.classList.remove(CLASSE_SPLASH_ATIVA);
    this.configAtual.set(null);
  }

  private tentarEncerrar(): void {
    if (this.liberada && this.tempoMinimoAtingido) this.encerrar();
  }

  private agendar(acao: () => void, ms: number): void {
    this.timers.push(setTimeout(acao, ms));
  }

  private limparTimers(): void {
    this.timers.forEach(clearTimeout);
    this.timers = [];
  }
}

/** Centro de um elemento na tela, para servir de origem às ondas de cor. */
export function centroDoElemento(elemento?: Element | null): SplashConfig['origem'] {
  if (!elemento) return undefined;
  const { left, top, width, height } = elemento.getBoundingClientRect();
  return { x: left + width / 2, y: top + height / 2 };
}
