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

/** Quanto tempo a comemoração fica na tela antes de o diálogo fechar sozinho. */
export const TEMPO_COMEMORACAO_MS = 1800;

/** Peças de confete da comemoração: ângulo, distância, cor e atraso de cada uma. */
const CORES_CONFETE = ['var(--primary-light)', 'var(--accent-gold)', '#ffffff', 'var(--primary)'];
const CONFETES = Array.from({ length: 18 }, (_, i) => ({
  angulo: (360 / 18) * i + (i % 2 ? 8 : -8),
  distancia: 90 + (i % 3) * 28,
  cor: CORES_CONFETE[i % CORES_CONFETE.length],
  atraso: (i % 4) * 40,
}));

const PASSOS = ['Boas-vindas', 'Como funciona', 'Seu saldo'] as const;

/** Uma dica por passo, mostrada na lateral para ela não ficar vazia. */
const DICAS = [
  { titulo: 'Leva menos de 1 minuto', texto: 'São só três telas. Dá para voltar a qualquer momento.' },
  { titulo: 'Tudo em um lugar só', texto: 'Cada movimentação que você registra já atualiza o saldo e os relatórios.' },
  { titulo: 'Não precisa ser exato', texto: 'Use o valor aproximado da sua conta hoje. As próximas movimentações acertam o resto.' },
] as const;

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
  readonly confetes = CONFETES;
  readonly dicas = DICAS;

  readonly passoAtual = signal(0);
  readonly centavos = signal(0);
  readonly enviando = signal(false);
  readonly tentouSalvar = signal(false);
  readonly erro = signal<string | null>(null);
  readonly fechando = signal(false);
  /** Para que lado o passo novo entra (a animação acompanha o sentido da navegação). */
  readonly direcao = signal<'frente' | 'tras'>('frente');
  /** Carteira criada: mostra a comemoração antes de fechar. */
  readonly comemorando = signal(false);
  /** Incrementa a cada tentativa inválida, para a animação de "tremida" tocar de novo. */
  readonly tremidas = signal(0);

  readonly ultimoPasso = computed(() => this.passoAtual() === PASSOS.length - 1);
  /** Progresso da linha que liga as etapas, de 0 a 1. */
  readonly progresso = computed(() => this.passoAtual() / (PASSOS.length - 1));
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
    if (this.ultimoPasso()) return;
    this.direcao.set('frente');
    this.passoAtual.update((p) => p + 1);
  }

  voltar(): void {
    if (this.passoAtual() === 0 || this.enviando()) return;
    this.direcao.set('tras');
    this.passoAtual.update((p) => p - 1);
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
    if (!this.saldoValido()) {
      this.tremidas.update((n) => n + 1);
      this.tremerCampo();
      return;
    }

    this.enviando.set(true);
    this.carteiraService.criarCarteira(this.centavos() / 100).subscribe({
      next: (carteira) => {
        this.enviando.set(false);
        this.comemorando.set(true);
        this.timerFechar = setTimeout(
          () => this.encerrar(() => this.concluido.emit(carteira)),
          TEMPO_COMEMORACAO_MS,
        );
      },
      error: (erro: HttpErrorResponse) => {
        this.enviando.set(false);
        this.erro.set(mensagemErroCarteira(erro));
      },
    });
  }

  fechar(): void {
    // Não fecha no meio do envio: o usuário perderia o retorno da criação da carteira
    if (this.enviando() || this.fechando() || this.comemorando()) return;
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

  /** Web Animations API: toca a tremida de novo a cada tentativa, sem recriar o campo (e perder o foco). */
  private tremerCampo(): void {
    const caixa = this.campoSaldo()?.nativeElement.closest<HTMLElement>('.saldo');
    caixa?.animate?.(
      [
        { transform: 'translateX(0)' },
        { transform: 'translateX(-8px)' },
        { transform: 'translateX(7px)' },
        { transform: 'translateX(-4px)' },
        { transform: 'translateX(0)' },
      ],
      { duration: 380, easing: 'ease-out' },
    );
  }

  private encerrar(depois: () => void): void {
    this.fechando.set(true);
    this.timerFechar = setTimeout(depois, TEMPO_FECHAR_MS);
  }
}
