import {
  afterNextRender,
  Component,
  computed,
  DestroyRef,
  effect,
  ElementRef,
  inject,
  output,
  signal,
  viewChild,
} from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { CarteiraService } from '../../services/carteira/carteira-service';
import { CarteiraResponse } from '../../interfaces/carteira/Carteira';
import { mensagemErroCarteira } from '../../utils/mensagens-erro';
import { centavosParaTexto, extrairCentavos } from '../../utils/moeda';

/** O backend exige saldo inicial de pelo menos R$ 1,00 (WalletRequestDTO: @Min(1)). */
export const SALDO_MINIMO_CENTAVOS = 100;

/** Duração da animação de saída do diálogo (ver .onboarding--fechando no SCSS). */
export const TEMPO_FECHAR_MS = 200;

const PASSOS = ['Boas-vindas', 'Como funciona', 'Seu saldo'] as const;

/**
 * Configuração inicial: apresenta o app e cria a carteira com o saldo informado.
 * Usa <dialog> nativo: o navegador cuida do foco preso no diálogo, do Esc
 * e de deixar o resto da página inerte.
 */
@Component({
  selector: 'app-onboarding-component',
  templateUrl: './onboarding-component.html',
  styleUrl: './onboarding-component.scss',
})
export class OnboardingComponent {
  private readonly carteiraService = inject(CarteiraService);
  private readonly dialogo = viewChild.required<ElementRef<HTMLDialogElement>>('dialogo');
  private readonly campoSaldo = viewChild<ElementRef<HTMLInputElement>>('campoSaldo');
  private timerFechar: ReturnType<typeof setTimeout> | null = null;

  /** Carteira criada com sucesso. */
  readonly concluido = output<CarteiraResponse>();
  /** Usuário fechou sem concluir. */
  readonly fechado = output<void>();

  readonly passos = PASSOS;
  readonly sugestoes = [500, 1000, 5000];

  readonly passoAtual = signal(0);
  readonly centavos = signal(0);
  readonly enviando = signal(false);
  readonly tentouSalvar = signal(false);
  readonly erro = signal<string | null>(null);
  readonly fechando = signal(false);

  readonly ultimoPasso = computed(() => this.passoAtual() === PASSOS.length - 1);
  readonly saldoTexto = computed(() => centavosParaTexto(this.centavos()));
  readonly saldoValido = computed(() => this.centavos() >= SALDO_MINIMO_CENTAVOS);
  readonly mostrarErroSaldo = computed(() => this.tentouSalvar() && !this.saldoValido());

  constructor() {
    afterNextRender(() => {
      const dialogo = this.dialogo().nativeElement;
      // showModal não existe em ambientes de teste antigos: cai no atributo open
      if (typeof dialogo.showModal === 'function') dialogo.showModal();
      else dialogo.setAttribute('open', '');
    });
    // Ao chegar no passo do saldo, o campo já recebe o foco
    effect(() => this.campoSaldo()?.nativeElement.focus());
    inject(DestroyRef).onDestroy(() => {
      if (this.timerFechar) clearTimeout(this.timerFechar);
    });
  }

  avancar(): void {
    if (!this.ultimoPasso()) this.passoAtual.update((p) => p + 1);
  }

  voltar(): void {
    if (this.passoAtual() > 0 && !this.enviando()) this.passoAtual.update((p) => p - 1);
  }

  /** Máscara de moeda: só dígitos entram, e o valor "cresce" a partir dos centavos. */
  aoDigitarSaldo(evento: Event): void {
    const campo = evento.target as HTMLInputElement;
    this.centavos.set(extrairCentavos(campo.value));
    // Reescreve o campo na hora: letras e símbolos nunca chegam a aparecer
    campo.value = this.saldoTexto();
    this.erro.set(null);
  }

  selecionarSugestao(reais: number): void {
    this.centavos.set(reais * 100);
    this.erro.set(null);
  }

  salvar(evento?: Event): void {
    evento?.preventDefault();
    if (this.enviando()) return;

    this.tentouSalvar.set(true);
    this.erro.set(null);
    if (!this.saldoValido()) return;

    this.enviando.set(true);
    this.carteiraService.criarCarteira(this.centavos() / 100).subscribe({
      next: (carteira) => this.encerrar(() => this.concluido.emit(carteira)),
      error: (erro: HttpErrorResponse) => {
        this.enviando.set(false);
        this.erro.set(mensagemErroCarteira(erro));
      },
    });
  }

  fechar(): void {
    // Não fecha no meio do envio: o usuário perderia o retorno da criação da carteira
    if (this.enviando() || this.fechando()) return;
    this.encerrar(() => this.fechado.emit());
  }

  /** Esc: o navegador fecharia o <dialog> sozinho; aqui passa pela nossa animação e regras. */
  aoCancelar(evento: Event): void {
    evento.preventDefault();
    this.fechar();
  }

  /** Clique no fundo escurecido (o alvo é o próprio <dialog>, não o conteúdo). */
  aoClicarFundo(evento: MouseEvent): void {
    if (evento.target === this.dialogo().nativeElement) this.fechar();
  }

  private encerrar(depois: () => void): void {
    this.fechando.set(true);
    this.timerFechar = setTimeout(depois, TEMPO_FECHAR_MS);
  }
}
