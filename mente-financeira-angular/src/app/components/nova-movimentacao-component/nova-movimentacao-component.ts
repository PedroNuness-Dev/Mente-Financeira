import {
  afterNextRender,
  Component,
  computed,
  DestroyRef,
  ElementRef,
  inject,
  input,
  output,
  signal,
  viewChild,
} from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
import { Observable } from 'rxjs';
import { CarteiraService } from '../../services/carteira/carteira-service';
import { CarteiraResponse } from '../../interfaces/carteira/Carteira';
import { TipoMovimentacao } from '../../interfaces/movimentacao/Movimentacao';
import { CATEGORIAS_ENTRADA, CATEGORIAS_SAIDA, rotuloCategoria } from '../../utils/categorias';
import { iconeCategoria } from '../../utils/icones-categoria';
import { centavosParaTexto, extrairCentavos, MAX_CENTAVOS } from '../../utils/moeda';
import { mensagemErroMovimentacao } from '../../utils/mensagens-erro';
import { formatarMoeda } from '../../utils/formatar-moeda';

/** O backend exige pelo menos R$ 1,00 (@Min(1)) e a coluna description é VARCHAR(255). */
export const VALOR_MINIMO_CENTAVOS = 100;
export const MAX_DESCRICAO = 255;
const TEMPO_FECHAR_MS = 200;

/** Ignora acentos e maiúsculas na busca: "saude" encontra "Saúde". */
function normalizar(texto: string): string {
  return texto.normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase().trim();
}

/**
 * Diálogo de depósito (PUT /wallet/deposit) ou de retirada (PUT /wallet/withdraw).
 * O tipo é fixo por abertura: cada botão da tela abre o seu diálogo.
 * Usa <dialog> nativo, como o onboarding.
 */
@Component({
  selector: 'app-nova-movimentacao-component',
  imports: [ReactiveFormsModule],
  templateUrl: './nova-movimentacao-component.html',
  styleUrl: './nova-movimentacao-component.scss',
})
export class NovaMovimentacaoComponent {
  private readonly carteiraService = inject(CarteiraService);
  private readonly dialogo = viewChild.required<ElementRef<HTMLDialogElement>>('dialogo');
  private readonly campoValor = viewChild.required<ElementRef<HTMLInputElement>>('campoValor');
  private timer: ReturnType<typeof setTimeout> | null = null;

  /** ENTRADA = depósito, RETIRADA = retirada. */
  readonly tipo = input.required<TipoMovimentacao>();
  /** Saldo atual, para avisar quando uma retirada deixaria a carteira negativa. */
  readonly saldoAtual = input<number | null>(null);

  readonly concluido = output<CarteiraResponse>();
  readonly fechado = output<void>();

  readonly centavos = signal(0);
  readonly enviando = signal(false);
  readonly tentouSalvar = signal(false);
  readonly erro = signal<string | null>(null);
  readonly fechando = signal(false);
  readonly busca = signal('');

  readonly form = new FormGroup({
    categoria: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    descricao: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(MAX_DESCRICAO), Validators.pattern(/\S/)],
    }),
  });

  readonly maxDescricao = MAX_DESCRICAO;
  /** Atalhos que somam ao valor digitado (em reais). */
  readonly atalhos = [10, 50, 100, 500];

  private readonly categoriaAtual = toSignal(this.form.controls.categoria.valueChanges, { initialValue: '' });
  private readonly descricaoAtual = toSignal(this.form.controls.descricao.valueChanges, { initialValue: '' });
  readonly tamanhoDescricao = computed(() => this.descricaoAtual().length);
  readonly categoriaSelecionada = computed(() => this.categorias().find((c) => c.valor === this.categoriaAtual()) ?? null);
  /** Muda a cada alteração do valor, para a animação de "pulso" tocar de novo. */
  readonly pulsos = signal(0);
  readonly entrada = computed(() => this.tipo() === 'ENTRADA');
  readonly categorias = computed(() =>
    (this.entrada() ? CATEGORIAS_ENTRADA : CATEGORIAS_SAIDA).map((valor) => ({ valor, rotulo: rotuloCategoria(valor), icone: iconeCategoria(valor) })),
  );
  readonly categoriasFiltradas = computed(() => {
    const termo = normalizar(this.busca());
    return termo ? this.categorias().filter((c) => normalizar(c.rotulo).includes(termo)) : this.categorias();
  });
  readonly valorTexto = computed(() => centavosParaTexto(this.centavos()));
  readonly valorValido = computed(() => this.centavos() >= VALOR_MINIMO_CENTAVOS);
  readonly mostrarErroValor = computed(() => this.tentouSalvar() && !this.valorValido());

  /** Saldo depois da movimentação (null quando não sabemos o saldo atual). */
  readonly saldoDepois = computed(() => {
    const saldo = this.saldoAtual();
    if (saldo === null) return null;
    const valor = this.centavos() / 100;
    return this.entrada() ? saldo + valor : saldo - valor;
  });

  /** Saldo depois da retirada, quando ficaria negativo (o aviso não bloqueia o envio). */
  readonly saldoNegativo = computed(() => {
    const depois = this.saldoDepois();
    if (this.entrada() || depois === null || !this.valorValido()) return null;
    return depois < 0 ? formatarMoeda(depois) : null;
  });

  readonly formatarMoeda = formatarMoeda;

  constructor() {
    afterNextRender(() => {
      const dialogo = this.dialogo().nativeElement;
      if (typeof dialogo.showModal === 'function') dialogo.showModal();
      else dialogo.setAttribute('open', '');
      this.campoValor().nativeElement.focus();
    });
    inject(DestroyRef).onDestroy(() => {
      if (this.timer) clearTimeout(this.timer);
    });
  }

  aoBuscar(evento: Event): void {
    this.busca.set((evento.target as HTMLInputElement).value);
  }

  aoDigitarValor(evento: Event): void {
    const campo = evento.target as HTMLInputElement;
    this.centavos.set(extrairCentavos(campo.value));
    campo.value = this.valorTexto();
    this.erro.set(null);
  }

  /** Soma um atalho ao valor atual (respeitando o máximo aceito). */
  somar(reais: number): void {
    if (this.enviando()) return;
    this.centavos.update((c) => Math.min(c + reais * 100, MAX_CENTAVOS));
    this.pulsos.update((n) => n + 1);
    this.erro.set(null);
  }

  zerar(): void {
    if (this.enviando()) return;
    this.centavos.set(0);
    this.campoValor().nativeElement.focus();
  }

  /** Os erros dos campos só aparecem depois da primeira tentativa de registrar. */
  campoInvalido(nome: 'categoria' | 'descricao'): boolean {
    return this.tentouSalvar() && this.form.controls[nome].invalid;
  }

  salvar(evento?: Event): void {
    evento?.preventDefault();
    if (this.enviando()) return;

    this.tentouSalvar.set(true);
    this.erro.set(null);
    if (!this.valorValido() || this.form.invalid) return;

    const valor = this.centavos() / 100;
    const { categoria, descricao } = this.form.getRawValue();
    const base = { movementCategory: categoria, description: descricao.trim() };
    const requisicao: Observable<CarteiraResponse> = this.entrada()
      ? this.carteiraService.depositar({ depositAmount: valor, ...base })
      : this.carteiraService.retirar({ withdrawalAmount: valor, ...base });

    this.enviando.set(true);
    this.form.disable();
    requisicao.subscribe({
      next: (resposta) => {
        this.enviando.set(false);
        this.encerrar(() => this.concluido.emit(resposta));
      },
      error: (erro: HttpErrorResponse) => {
        this.enviando.set(false);
        this.form.enable();
        this.erro.set(mensagemErroMovimentacao(erro));
      },
    });
  }

  fechar(): void {
    if (this.enviando() || this.fechando()) return;
    this.encerrar(() => this.fechado.emit());
  }

  aoCancelar(evento: Event): void {
    evento.preventDefault();
    this.fechar();
  }

  aoClicarFundo(evento: MouseEvent): void {
    if (evento.target === this.dialogo().nativeElement) this.fechar();
  }

  private encerrar(depois: () => void): void {
    this.fechando.set(true);
    this.timer = setTimeout(depois, TEMPO_FECHAR_MS);
  }
}
