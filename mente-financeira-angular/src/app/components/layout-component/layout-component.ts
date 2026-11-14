import { Component, OnInit, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { UsuarioService } from '../../services/usuario/usuario-service';
import { AuthService } from '../../services/auth/auth-service';
import { ThemeService } from '../../services/theme/theme-service';
import { SplashService } from '../../services/splash/splash-service';
import { formatarDataBr } from '../../utils/formatar-data';

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

  get dataFormatada(): string {
    return formatarDataBr(this.usuario()?.creationDate);
  }
}