import { afterNextRender, Component, DestroyRef, ElementRef, inject, viewChild } from '@angular/core';
import { SplashService } from '../../services/splash/splash-service';

/**
 * Lado ilustrativo das telas de autenticação: manchete, mascote e
 * cards de status decorativos com leve parallax que segue o mouse.
 */
@Component({
  selector: 'app-auth-hero',
  templateUrl: './auth-hero-component.html',
  styleUrl: './auth-hero-component.scss',
})
export class AuthHeroComponent {
  private readonly splash = inject(SplashService);
  private readonly mascote = viewChild.required<ElementRef<HTMLImageElement>>('mascote');

  constructor() {
    // O mascote daqui é o ponto de partida do "voo" até o centro da splash de boas-vindas
    afterNextRender(() => this.splash.registrarMascoteOrigem(this.mascote().nativeElement));
    inject(DestroyRef).onDestroy(() => this.splash.registrarMascoteOrigem(null));
  }

  protected moverParallax(evento: PointerEvent): void {
    if (evento.pointerType !== 'mouse') return;

    const alvo = evento.currentTarget as HTMLElement;
    const { left, top, width, height } = alvo.getBoundingClientRect();
    // -0.5 .. 0.5 a partir do centro da área
    const x = (evento.clientX - left) / width - 0.5;
    const y = (evento.clientY - top) / height - 0.5;

    alvo.style.setProperty('--px', x.toFixed(3));
    alvo.style.setProperty('--py', y.toFixed(3));
  }

  protected resetarParallax(evento: PointerEvent): void {
    const alvo = evento.currentTarget as HTMLElement;
    alvo.style.setProperty('--px', '0');
    alvo.style.setProperty('--py', '0');
  }
}
