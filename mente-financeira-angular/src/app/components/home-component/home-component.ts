import { Component, computed, effect, inject, OnInit, signal } from '@angular/core';
import { SplashService } from '../../services/splash/splash-service';
import { RouterLink } from '@angular/router';
import { catchError, forkJoin, of } from 'rxjs';
import { CarteiraService } from '../../services/carteira/carteira-service';
import { UsuarioService } from '../../services/usuario/usuario-service';
import { MovimentacaoService } from '../../services/movimentacao/movimentacao-service';
import { AnaliseMensal } from '../../interfaces/movimentacao/AnaliseMensal';
import { Movimentacao } from '../../interfaces/movimentacao/Movimentacao';
import { OnboardingComponent } from '../onboarding-component/onboarding-component';
import { ContagemMoedaDirective } from '../../directives/contagem-moeda.directive';
import { formatarMoeda } from '../../utils/formatar-moeda';
import { formatarDataCurta, formatarDataPorExtenso } from '../../utils/formatar-data';
import { rotuloCategoria } from '../../utils/categorias';

type Estado = 'carregando' | 'erro' | 'pronto';

const MAX_CATEGORIAS = 6;
const QTD_ULTIMAS = 5;

/**
 * Visão geral: resumo financeiro do mês atual.
 * A existência da carteira (GET /api/user/wallet/check) decide entre
 * o convite de configuração inicial e o painel completo.
 */
@Component({
  selector: 'app-home-component',
  imports: [OnboardingComponent, ContagemMoedaDirective, RouterLink],
  templateUrl: './home-component.html',
  styleUrl: './home-component.scss',
})
export class HomeComponent implements OnInit {
  private readonly usuarioService = inject(UsuarioService);
  private readonly movimentacaoService = inject(MovimentacaoService);
  private readonly carteiraService = inject(CarteiraService);
  private readonly splash = inject(SplashService);
  private readonly hoje = new Date();
  private onboardingJaAbriu = false;

  readonly usuario = this.usuarioService.usuario;
  readonly estado = signal<Estado>('carregando');
  readonly analise = signal<AnaliseMensal | null>(null);
  /** null = não foi possível carregar (a seção mostra um aviso em vez de quebrar a tela). */
  readonly ultimas = signal<Movimentacao[] | null>([]);
  readonly mostrarOnboarding = signal(false);
  readonly temCarteira = signal(false);

  readonly dataPorExtenso = formatarDataPorExtenso(this.hoje);
  readonly nomeMes = this.hoje.toLocaleDateString('pt-BR', { month: 'long' });

  readonly saldo = computed(() => this.analise()?.currentBalance ?? 0);
  readonly entradas = computed(() => this.analise()?.income ?? 0);
  readonly saidas = computed(() => this.analise()?.withdrawal ?? 0);
  /** Resultado líquido do mês: o que entrou menos o que saiu. */
  readonly resultado = computed(() => this.entradas() - this.saidas());

  readonly situacao = computed(() => {
    const resultado = this.resultado();
    if (resultado > 0) return { rotulo: 'Positivo', tom: 'var(--primary-light)' } as const;
    if (resultado < 0) return { rotulo: 'Negativo', tom: 'var(--danger)' } as const;
    return { rotulo: 'No zero a zero', tom: 'var(--accent-gold)' } as const;
  });

  /** Quanto das entradas do mês já foi gasto (0 a 100+; null sem entradas). */
  readonly percentualGasto = computed(() => {
    const entradas = this.entradas();
    return entradas > 0 ? Math.round((this.saidas() / entradas) * 100) : null;
  });

  /** A cor do medidor acompanha a gravidade: tranquilo, atenção, estourou. */
  readonly tomUso = computed(() => {
    const uso = this.percentualGasto() ?? 0;
    if (uso > 100) return 'var(--danger)';
    if (uso >= 80) return 'var(--accent-gold)';
    return 'var(--primary-light)';
  });

  /** Saídas por categoria, da maior para a menor. */
  readonly gastosPorCategoria = computed(() =>
    (this.analise()?.categoryBreakdown ?? [])
      .filter((item) => item.movementType === 'RETIRADA')
      .sort((a, b) => b.totalAmount - a.totalAmount)
      .slice(0, MAX_CATEGORIAS)
      .map((item) => ({
        nome: rotuloCategoria(item.category),
        valor: formatarMoeda(item.totalAmount),
        percentual: Math.round(item.percentage),
      })),
  );

  readonly ultimasFormatadas = computed(() =>
    (this.ultimas() ?? []).map((mov) => {
      const entrada = mov.movementType === 'ENTRADA';
      return {
        id: mov.id,
        entrada,
        categoria: rotuloCategoria(mov.movementCategory),
        data: formatarDataCurta(mov.executionDate),
        valor: `${entrada ? '+' : '−'} ${formatarMoeda(mov.movedAmount)}`,
      };
    }),
  );

  constructor() {
    // Sem carteira, a configuração inicial abre sozinha (uma vez), mas só depois da
    // splash: o <dialog> fica na camada mais alta da página e apareceria por cima dela.
    effect(() => {
      const precisaConfigurar = this.estado() === 'pronto' && !this.temCarteira();
      if (precisaConfigurar && !this.splash.ativa() && !this.onboardingJaAbriu) {
        this.onboardingJaAbriu = true;
        this.mostrarOnboarding.set(true);
      }
    });
  }

  get primeiroNome(): string {
    return this.usuario()?.name.split(' ')[0] ?? '';
  }

  get saudacao(): string {
    const hora = this.hoje.getHours();
    if (hora < 12) return 'Bom dia';
    if (hora < 18) return 'Boa tarde';
    return 'Boa noite';
  }

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    // Só mostra o esqueleto no primeiro carregamento; ao atualizar, mantém o painel na tela
    if (!this.analise()) this.estado.set('carregando');

    forkJoin({
      check: this.carteiraService.verificarCarteira(),
      analise: this.movimentacaoService.analisarMes(this.hoje.getMonth() + 1, this.hoje.getFullYear()),
    }).subscribe({
      next: ({ check, analise }) => {
        this.temCarteira.set(check.haveWallet);
        this.analise.set(analise);
        this.estado.set('pronto');
        if (check.haveWallet) this.carregarUltimas();
      },
      error: () => this.estado.set('erro'),
    });
  }

  aoCriarCarteira(): void {
    this.mostrarOnboarding.set(false);
    this.carregar();
  }

  private carregarUltimas(): void {
    this.movimentacaoService
      .buscarUltimas(QTD_ULTIMAS)
      .pipe(catchError(() => of(null)))
      .subscribe((movimentacoes) => this.ultimas.set(movimentacoes));
  }
}
