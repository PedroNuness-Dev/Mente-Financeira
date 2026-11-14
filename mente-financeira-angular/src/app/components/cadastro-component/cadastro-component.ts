import { Component, computed, ElementRef, inject, signal, viewChild } from '@angular/core';
import { centroDoElemento, SplashService } from '../../services/splash/splash-service';
import { toSignal } from '@angular/core/rxjs-interop';
import { HttpErrorResponse } from '@angular/common/http';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { catchError, forkJoin, map, of, switchMap, tap, timer } from 'rxjs';
import { UsuarioService } from '../../services/usuario/usuario-service';
import { AuthService } from '../../services/auth/auth-service';
import { LoginRequest } from '../../interfaces/auth/LoginRequest';
import { LoginNavigationState } from '../login-component/login-component';
import { mensagemErroCadastro } from '../../utils/mensagens-erro';
import { EMAIL_PATTERN, SENHA_PATTERN, SENHA_TAMANHO_MINIMO, senhasIguais } from '../../utils/validadores';

type Etapa = 'formulario' | 'enviando' | 'concluido';
type CampoCadastro = 'nome' | 'email' | 'senha';

/** Tempo mínimo da tela de "conta criada", para a transição não parecer um piscar. */
export const TEMPO_MINIMO_CELEBRACAO_MS = 1200;

@Component({
  selector: 'app-cadastro-component',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './cadastro-component.html',
  styleUrl: './cadastro-component.scss',
  host: { class: 'mf-stagger' },
})
export class CadastroComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly usuarioService = inject(UsuarioService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly splash = inject(SplashService);
  private readonly iconeSucesso = viewChild<ElementRef<HTMLElement>>('iconeSucesso');

  readonly form = this.fb.group(
    {
      nome: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email, Validators.pattern(EMAIL_PATTERN)]],
      senha: [
        '',
        [Validators.required, Validators.minLength(SENHA_TAMANHO_MINIMO), Validators.pattern(SENHA_PATTERN)],
      ],
      confirmarSenha: ['', Validators.required],
    },
    { validators: senhasIguais('senha', 'confirmarSenha') },
  );

  readonly etapa = signal<Etapa>('formulario');
  readonly erroCadastro = signal<string | null>(null);
  readonly primeiroNome = signal('');
  readonly mostrarSenha = signal(false);
  readonly enviado = signal(false);
  readonly senhaFocada = signal(false);

  private readonly senha = toSignal(this.form.controls.senha.valueChanges, { initialValue: '' });

  /** Checklist ao vivo dos requisitos da senha. */
  readonly requisitosSenha = computed(() => {
    const senha = this.senha();
    return [
      { rotulo: `${SENHA_TAMANHO_MINIMO} ou mais caracteres`, atendido: senha.length >= SENHA_TAMANHO_MINIMO },
      { rotulo: 'Uma letra', atendido: /[A-Za-z]/.test(senha) },
      { rotulo: 'Um número', atendido: /\d/.test(senha) },
    ];
  });

  /** 0 a 3: quantos requisitos já foram atendidos. */
  readonly forcaSenha = computed(() => this.requisitosSenha().filter((r) => r.atendido).length);

  readonly mostrarRequisitos = computed(
    () => this.senhaFocada() || this.senha().length > 0 || this.enviado(),
  );

  alternarSenha(): void {
    this.mostrarSenha.update((valor) => !valor);
  }

  /** Erro aparece se o campo é inválido e o usuário já saiu dele ou tentou enviar. */
  mostrarErro(campo: CampoCadastro): boolean {
    const controle = this.form.controls[campo];
    return controle.invalid && (controle.touched || this.enviado());
  }

  confirmacaoInvalida(): boolean {
    const controle = this.form.controls.confirmarSenha;
    const invalida = controle.invalid || this.form.hasError('senhasDiferentes');
    return invalida && (controle.touched || this.enviado());
  }

  confirmacaoConfere(): boolean {
    const { senha, confirmarSenha } = this.form.controls;
    return senha.valid && !!confirmarSenha.value && !this.form.hasError('senhasDiferentes');
  }

  criarConta(): void {
    if (this.etapa() !== 'formulario') return;

    this.enviado.set(true);
    this.erroCadastro.set(null);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { nome, email, senha } = this.form.getRawValue();
    const credenciais: LoginRequest = { email: email.trim(), password: senha };
    this.etapa.set('enviando');

    this.usuarioService
      .cadastrarUsuario({ name: nome.trim(), ...credenciais })
      .pipe(
        tap((usuario) => {
          this.primeiroNome.set(usuario.name.trim().split(' ')[0]);
          this.etapa.set('concluido');
        }),
        // Conta criada: entra automaticamente, respeitando um tempo mínimo de celebração.
        // Se o login automático falhar, a conta existe — o usuário só precisa entrar manualmente.
        switchMap(() =>
          forkJoin([
            this.authService.login(credenciais).pipe(
              map(() => true),
              catchError(() => of(false)),
            ),
            timer(TEMPO_MINIMO_CELEBRACAO_MS),
          ]),
        ),
      )
      .subscribe({
        next: ([logado]) => (logado ? this.irParaApp() : this.irParaLogin(credenciais.email)),
        error: (erro: HttpErrorResponse) => {
          this.etapa.set('formulario');
          this.erroCadastro.set(mensagemErroCadastro(erro));
        },
      });
  }

  /** Splash de boas-vindas partindo do ícone de sucesso e, com a tela coberta, troca para o app. */
  private async irParaApp(): Promise<void> {
    await this.splash.mostrar({
      titulo: `Tudo pronto, ${this.primeiroNome()}!`,
      subtitulo: 'Sua jornada financeira começa agora.',
      origem: centroDoElemento(this.iconeSucesso()?.nativeElement),
    });

    // replaceUrl: o cadastro sai do histórico, então "voltar" não reabre o formulário
    const navegou = await this.router.navigate(['/home'], { replaceUrl: true });
    if (!navegou) this.splash.encerrar();
  }

  private irParaLogin(email: string): void {
    const state: LoginNavigationState = {
      email,
      aviso: 'Sua conta foi criada! Entre com seu e-mail e senha para continuar.',
    };
    this.router.navigate(['/auth/login'], { state });
  }
}
