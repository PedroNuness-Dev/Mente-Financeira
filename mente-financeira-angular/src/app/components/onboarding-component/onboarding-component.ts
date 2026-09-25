import { Component, computed, output, signal } from '@angular/core';

@Component({
  selector: 'app-onboarding-component',
  imports: [],
  templateUrl: './onboarding-component.html',
  styleUrl: './onboarding-component.scss',
})
export class OnboardingComponent {
  concluido = output<number>();
  fechado = output<void>();

  readonly totalPassos = 4;
  readonly sugestoes = [500, 1000, 5000];

  passoAtual = signal(0);
  saldoTexto = signal('');

  progresso = computed(() => ((this.passoAtual() + 1) / this.totalPassos) * 100);

  get saldoNumero(): number {
    const normalizado = this.saldoTexto().replace(',', '.').trim();
    return Number(normalizado);
  }

  get saldoValido(): boolean {
    return this.saldoTexto().trim() !== '' && !Number.isNaN(this.saldoNumero) && this.saldoNumero >= 0;
  }

  avancar(): void {
    if (this.passoAtual() < this.totalPassos - 1) {
      this.passoAtual.update((p) => p + 1);
    }
  }

  voltar(): void {
    if (this.passoAtual() > 0) {
      this.passoAtual.update((p) => p - 1);
    }
  }

  onInputSaldo(valor: string): void {
    this.saldoTexto.set(valor);
  }

  selecionarSugestao(valor: number): void {
    this.saldoTexto.set(String(valor));
  }

  salvar(): void {
    if (!this.saldoValido) return;
    this.concluido.emit(this.saldoNumero);
  }

  fechar(): void {
    this.fechado.emit();
  }
}