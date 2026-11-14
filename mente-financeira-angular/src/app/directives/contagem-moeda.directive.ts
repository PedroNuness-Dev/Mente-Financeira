import { DOCUMENT } from '@angular/common';
import { DestroyRef, Directive, effect, ElementRef, inject, input } from '@angular/core';
import { formatarMoeda } from '../utils/formatar-moeda';

const DURACAO_MS = 900;

/**
 * Exibe um valor em reais e, quando ele muda, "conta" do valor anterior até o novo.
 * Uso: <strong [appContagemMoeda]="saldo()"></strong>
 * Sem animação quando o usuário pede movimento reduzido (ou fora do navegador).
 */
@Directive({
  selector: '[appContagemMoeda]',
})
export class ContagemMoedaDirective {
  readonly valor = input.required<number>({ alias: 'appContagemMoeda' });

  private readonly elemento: HTMLElement = inject(ElementRef).nativeElement;
  private readonly view = inject(DOCUMENT).defaultView;
  private exibido = 0;
  private quadro: number | null = null;

  constructor() {
    effect(() => this.animarAte(this.valor()));
    inject(DestroyRef).onDestroy(() => this.cancelar());
  }

  private animarAte(destino: number): void {
    this.cancelar();
    const view = this.view;
    const semAnimacao =
      !view ||
      typeof view.requestAnimationFrame !== 'function' ||
      view.matchMedia?.('(prefers-reduced-motion: reduce)').matches;

    if (semAnimacao || destino === this.exibido) {
      this.mostrar(destino);
      return;
    }

    const origem = this.exibido;
    const inicio = view.performance.now();
    const passo = (agora: number) => {
      const t = Math.min(Math.max((agora - inicio) / DURACAO_MS, 0), 1);
      const suavizado = 1 - Math.pow(1 - t, 4); // ease-out
      this.mostrar(t === 1 ? destino : origem + (destino - origem) * suavizado);
      this.quadro = t < 1 ? view.requestAnimationFrame(passo) : null;
    };
    this.quadro = view.requestAnimationFrame(passo);
  }

  private mostrar(valor: number): void {
    this.exibido = valor;
    this.elemento.textContent = formatarMoeda(valor);
  }

  private cancelar(): void {
    if (this.quadro !== null) this.view?.cancelAnimationFrame(this.quadro);
    this.quadro = null;
  }
}
