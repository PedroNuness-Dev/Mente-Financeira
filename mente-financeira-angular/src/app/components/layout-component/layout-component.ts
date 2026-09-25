import { Component, OnInit, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { UsuarioService } from '../../services/usuario/usuario-service';
import { AuthService } from '../../services/auth/auth-service';
import { formatarDataBr } from '../../utils/formatar-data';

const TEMA_KEY = 'tema';

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

  usuario = this.usuarioService.usuario;
  carregando = signal(true);
  erro = signal(false);
  mostrarPerfil = signal(false);
  tema = signal<'claro' | 'escuro'>(this.lerTemaSalvo());

  ngOnInit(): void {
    this.usuarioService.buscarPerfilDoUsuarioAutenticado().subscribe({
      next: () => this.carregando.set(false),
      error: () => {
        this.erro.set(true);
        this.carregando.set(false);
      },
    });
  }

  sair(): void {
    this.authService.logout();
    this.router.navigate(['/auth/login']);
  }

  alternarTema(): void {
    const novoTema = this.tema() === 'claro' ? 'escuro' : 'claro';
    this.tema.set(novoTema);
    localStorage.setItem(TEMA_KEY, novoTema);
  }

  private lerTemaSalvo(): 'claro' | 'escuro' {
    return localStorage.getItem(TEMA_KEY) === 'escuro' ? 'escuro' : 'claro';
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