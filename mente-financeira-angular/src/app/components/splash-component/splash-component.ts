import { afterRenderEffect, Component, computed, ElementRef, inject, viewChild } from '@angular/core';
import { DOCUMENT } from '@angular/common';
import { SplashService } from '../../services/splash/splash-service';

/**
 * Tela de transição ao entrar no app: ondas nas cores da marca cobrem a tela
 * a partir do botão clicado, o mascote "voa" da posição dele no hero até o centro
 * (no mobile, surge com um pop) e uma saudação aparece abaixo.
 * Fica no App (fora do router-outlet), por isso sobrevive à troca de rota.
 */
@Component({
  selector: 'app-splash',
  templateUrl: './splash-component.html',
  styleUrl: './splash-component.scss',
})
export class SplashComponent {
  private readonly splash = inject(SplashService);
  private readonly document = inject(DOCUMENT);

  protected readonly config = this.splash.config;
  private readonly mascote = viewChild<ElementRef<HTMLImageElement>>('mascote');
  private ultimoAnimado: HTMLImageElement | null = null;

  /** Variáveis CSS com a origem das ondas e o raio que cobre a tela inteira. */
  protected readonly estiloOndas = computed(() => {
    const view = this.document.defaultView;
    const largura = view?.innerWidth ?? 0;
    const altura = view?.innerHeight ?? 0;
    const { x, y } = this.config()?.origem ?? { x: largura / 2, y: altura / 2 };
    const raio = Math.hypot(Math.max(x, largura - x), Math.max(y, altura - y));
    return { '--x': `${x}px`, '--y': `${y}px`, '--raio': `${Math.ceil(raio)}px` };
  });

  /** Sem mascote de origem visível (mobile), o mascote entra com pop em vez de voar. */
  protected readonly voa = computed(() => !!this.config() && !!this.splash.posicaoMascoteOrigem());

  constructor() {
    afterRenderEffect(() => {
      const img = this.mascote()?.nativeElement;
      if (img && img !== this.ultimoAnimado) {
        this.ultimoAnimado = img;
        this.voarDaOrigem(img);
      }
    });
  }

  /** FLIP: parte da posição/tamanho do mascote no hero e anima até a posição final. */
  private voarDaOrigem(img: HTMLImageElement): void {
    const origem = this.splash.posicaoMascoteOrigem();
    const reduzirMovimento = this.document.defaultView?.matchMedia?.('(prefers-reduced-motion: reduce)').matches;
    if (!origem || reduzirMovimento || typeof img.animate !== 'function') return;

    const destino = img.getBoundingClientRect();
    if (!destino.width) return;

    const dx = origem.left + origem.width / 2 - (destino.left + destino.width / 2);
    const dy = origem.top + origem.height / 2 - (destino.top + destino.height / 2);
    const escala = origem.width / destino.width;

    // Aparece em 120ms exatamente sobre o mascote original (mesma imagem), que some aos 150ms
    // (ver auth-hero-component.scss): a troca entre os dois não é percebida
    img.animate([{ opacity: 0 }, { opacity: 1 }], { duration: 120, easing: 'linear', fill: 'backwards' });
    img.animate(
      [{ transform: `translate(${dx}px, ${dy}px) scale(${escala})` }, { transform: 'none' }],
      { duration: 1100, easing: 'cubic-bezier(0.65, 0, 0.35, 1)', fill: 'backwards' },
    );
  }
}
