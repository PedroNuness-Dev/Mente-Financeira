import {
  afterNextRender,
  Component,
  DestroyRef,
  ElementRef,
  HostListener,
  OnInit,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { filter } from 'rxjs';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { UsuarioService } from '../../services/usuario/usuario-service';
import { AuthService } from '../../services/auth/auth-service';
import { ThemeService } from '../../services/theme/theme-service';
import { SplashService } from '../../services/splash/splash-service';
import { formatarDataBr, lerDataHora } from '../../utils/formatar-data';

/** Itens da navbar: o ícone é uma lista de "d" de <path> (traço 24x24). */
export const ITENS_NAV = [
  { rota: '/home', nome: 'Visão geral', icone: ['M3 8a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z', 'M3 10h18', 'M7 14h3'] },
  { rota: '/movimentacoes', nome: 'Movimentações', icone: ['M4 7h13M17 7l-3-3M17 7l-3 3', 'M20 17H7M7 17l3-3M7 17l3 3'] },
  { rota: '/categorias', nome: 'Categorias', icone: ['M12 3a9 9 0 1 0 9 9', 'M12 3v9l6.5 3.5', 'M15 3.5A9 9 0 0 1 20.5 9H15z'] },
  { rota: '/relatorios', nome: 'Relatórios', icone: ['M4 20V10', 'M12 20V4', 'M20 20v-7'] },
  { rota: '/configuracoes', nome: 'Ajustes', icone: ['M4 6h10M18 6h2', 'M16 4v4', 'M4 12h4M12 12h8', 'M10 10v4', 'M4 18h12M20 18h0', 'M18 16v4'] },
] as const;

/** Distância de rolagem a partir da qual a navbar fica compacta. */
const LIMITE_COMPACTO_PX = 24;

@Component({
  selector: 'app-layout-component',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './layout-component.html',
  styleUrl: './layout-component.scss',
})
export class LayoutComponent implements OnInit {
  private usuarioService = inject(UsuarioService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private themeService = inject(ThemeService);
  private splash = inject(SplashService);

  usuario = this.usuarioService.usuario;
  carregando = signal(true);
  erro = signal(false);
  mostrarPerfil = signal(false);
  tema = this.themeService.tema;

  readonly itens = ITENS_NAV;
  /** Posição e largura da pílula que marca o link ativo (ou o que está sob o mouse). */
  readonly pilula = signal({ x: 0, w: 0 });
  readonly rolou = signal(false);
  /** Pílula fraca que segue o mouse; visivel = false quando o mouse sai do trilho. */
  readonly sombra = signal({ x: 0, w: 0, visivel: false });
  private readonly trilho = viewChild<ElementRef<HTMLElement>>('trilho');

  constructor() {
    // A classe do routerLinkActive só muda depois da navegação: mede no quadro seguinte
    this.router.events
      .pipe(filter((e) => e instanceof NavigationEnd), takeUntilDestroyed(inject(DestroyRef)))
      .subscribe(() => {
        this.medirNoProximoQuadro();
        this.confirmarSelecao();
      });
    afterNextRender(() => this.medirNoProximoQuadro());
  }

  alternarPerfil(evento: MouseEvent): void {
    evento.stopPropagation();
    this.mostrarPerfil.update((aberto) => !aberto);
  }

  /** Clique fora do cartão de perfil fecha o cartão. */
  @HostListener('document:click', ['$event'])
  aoClicarNoDocumento(evento: MouseEvent): void {
    if (!this.mostrarPerfil()) return;
    const alvo = evento.target as HTMLElement | null;
    if (!alvo?.closest('.perfil')) this.mostrarPerfil.set(false);
  }

  @HostListener('document:keydown.escape')
  aoApertarEsc(): void {
    this.mostrarPerfil.set(false);
  }

  @HostListener('window:resize')
  aoRedimensionar(): void {
    this.medirAtivo();
  }

  @HostListener('window:scroll')
  aoRolar(): void {
    this.rolou.set(window.scrollY > LIMITE_COMPACTO_PX);
  }

  /**
   * Mouse sobre um link: a pílula fraca desliza até ele, e a forte fica no ativo.
   * Ao sair, a fraca volta para baixo da forte e some (o próximo hover parte dali).
   */
  espiar(evento: MouseEvent | null): void {
    const sobre = evento?.currentTarget as HTMLElement | undefined;
    const alvo = sobre ?? this.linkAtivo();
    if (alvo) this.sombra.set({ x: alvo.offsetLeft, w: alvo.offsetWidth, visivel: !!sobre });
    else this.sombra.update((s) => ({ ...s, visivel: false }));
  }

  /** Posiciona a pílula forte no link da página atual. */
  private medirAtivo(): void {
    const ativo = this.linkAtivo();
    this.pilula.set(ativo ? { x: ativo.offsetLeft, w: ativo.offsetWidth } : { x: 0, w: 0 });
    if (!this.sombra().visivel) this.espiar(null);
  }

  private linkAtivo(): HTMLElement | null {
    return this.trilho()?.nativeElement.querySelector<HTMLElement>('.trilho__link--ativo') ?? null;
  }

  /** Clicou: a pílula forte chega no novo link e dá um "aperto" de confirmação. */
  private confirmarSelecao(): void {
    requestAnimationFrame?.(() => {
      this.trilho()
        ?.nativeElement.querySelector<HTMLElement>('.trilho__pilula')
        ?.animate?.(
          [{ scale: '1 1' }, { scale: '0.9 0.82' }, { scale: '1.04 1.06' }, { scale: '1 1' }],
          { duration: 420, easing: 'cubic-bezier(0.16, 1, 0.3, 1)' },
        );
    });
  }

  private medirNoProximoQuadro(): void {
    if (typeof requestAnimationFrame === 'function') requestAnimationFrame(() => this.medirAtivo());
    else this.medirAtivo();
  }

  ngOnInit(): void {
    // Com o perfil carregado (ou com erro), a splash de boas-vindas pode sair
    this.usuarioService.buscarPerfilDoUsuarioAutenticado().subscribe({
      next: () => {
        this.carregando.set(false);
        this.splash.liberar();
      },
      error: () => {
        this.erro.set(true);
        this.carregando.set(false);
        this.splash.liberar();
      },
    });
  }

  sair(): void {
    this.authService.logout();
    // replaceUrl: o "voltar" do navegador não retorna para uma tela do app já deslogado
    this.router.navigate(['/auth/login'], { replaceUrl: true });
  }

  alternarTema(evento: MouseEvent): void {
    const botao = evento.currentTarget as HTMLElement;
    const { left, top, width, height } = botao.getBoundingClientRect();
    this.themeService.alternar({ x: left + width / 2, y: top + height / 2 });
  }

  get iniciais(): string {
    const nome = this.usuario()?.name ?? '';
    return nome
      .split(' ')
      .filter(Boolean)
      .slice(0, 2)
      .map((parte) => parte[0]?.toUpperCase())
      .join('');
  }

  /** "3 dias", "2 meses", "1 ano"... a partir da data de cadastro. */
  get tempoComoMembro(): string {
    const criacao = lerDataHora(this.usuario()?.creationDate);
    if (!criacao) return '—';
    const dias = Math.max(0, Math.floor((Date.now() - criacao.getTime()) / 86_400_000));
    if (dias < 1) return 'hoje';
    if (dias < 30) return dias === 1 ? '1 dia' : `${dias} dias`;
    const meses = Math.floor(dias / 30);
    if (meses < 12) return meses === 1 ? '1 mês' : `${meses} meses`;
    const anos = Math.floor(meses / 12);
    return anos === 1 ? '1 ano' : `${anos} anos`;
  }

  get dataFormatada(): string {
    return formatarDataBr(this.usuario()?.creationDate);
  }
}