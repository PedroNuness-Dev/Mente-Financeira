import { Component, ElementRef, inject, signal, viewChild } from '@angular/core';
import { centroDoElemento, SplashService } from '../../services/splash/splash-service';
import { HttpErrorResponse } from '@angular/common/http';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth/auth-service';
import { mensagemErroLogin } from '../../utils/mensagens-erro';
import { EMAIL_PATTERN } from '../../utils/validadores';

/** Estado opcional que outra tela pode enviar ao navegar para o login. */
export interface LoginNavigationState {
  email?: string;
  aviso?: string;
}

type CampoLogin = 'email' | 'senha';

@Component({
  selector: 'app-login-component',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login-component.html',
  styleUrl: './login-component.scss',
  host: { class: 'mf-stagger' },
})
export class LoginComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly splash = inject(SplashService);
  private readonly botaoEntrar = viewChild<ElementRef<HTMLButtonElement>>('botaoEntrar');

  readonly form = this.fb.group({
    email: ['', [Validators.required, Validators.email, Validators.pattern(EMAIL_PATTERN)]],
    senha: ['', Validators.required],
  });

  readonly mostrarSenha = signal(false);
  readonly enviado = signal(false);
  readonly carregando = signal(false);
  readonly erroLogin = signal<string | null>(null);
  readonly aviso = signal<string | null>(null);

  constructor() {
    const state = this.router.currentNavigation()?.extras.state as LoginNavigationState | undefined;
    if (state?.email) this.form.controls.email.setValue(state.email);
    if (state?.aviso) this.aviso.set(state.aviso);
  }

  alternarSenha(): void {
    this.mostrarSenha.update((valor) => !valor);
  }

  /** Erro só aparece depois que o usuário tenta entrar (sair do campo não basta). */
  mostrarErro(campo: CampoLogin): boolean {
    return this.form.controls[campo].invalid && this.enviado();
  }

  entrar(): void {
    if (this.carregando()) return;

    this.enviado.set(true);
    this.erroLogin.set(null);

    if (this.form.invalid) return;

    const { email, senha } = this.form.getRawValue();
    this.carregando.set(true);
    this.aviso.set(null);

    this.authService.login({ email: email.trim(), password: senha }).subscribe({
      next: (resposta) => this.entrarNoApp(resposta.name),
      error: (erro: HttpErrorResponse) => {
        this.carregando.set(false);
        this.erroLogin.set(mensagemErroLogin(erro));
      },
    });
  }

  /** Mostra a splash de boas-vindas e, com a tela já coberta, troca para o app. */
  private async entrarNoApp(nome?: string): Promise<void> {
    const primeiroNome = nome?.trim().split(' ')[0];

    await this.splash.mostrar({
      titulo: primeiroNome ? `Bem-vindo(a) de volta, ${primeiroNome}!` : 'Bem-vindo(a) de volta!',
      subtitulo: 'Preparando seu painel...',
      origem: centroDoElemento(this.botaoEntrar()?.nativeElement),
    });

    // replaceUrl: o login sai do histórico, então "voltar" não reabre o formulário
    const navegou = await this.router.navigate(['/home'], { replaceUrl: true });
    if (!navegou) {
      this.splash.encerrar();
      this.carregando.set(false);
    }
  }
}
