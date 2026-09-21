import { Component, inject, signal } from '@angular/core';
import { FormsModule, NgForm, NgModel } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { UsuarioService } from '../../services/usuario/usuario-service';
import { UsuarioRequest } from '../../interfaces/usuario/UsuarioRequest';

@Component({
  selector: 'app-cadastro-component',
  imports: [RouterLink, FormsModule],
  templateUrl: './cadastro-component.html',
  styleUrl: './cadastro-component.scss',
})
export class CadastroComponent {

  usuarioService = inject(UsuarioService);
  router = inject(Router);

  novoUsuario : UsuarioRequest | null = null;

  nome = '';
  email = '';
  senha = '';
  confirmarSenha = '';
  showPassword = signal(false);
  enviado = signal(false);
  senhaFocada = signal(false);
  confirmarFocada = signal(false);
  sucesso = signal(false);

  togglePassword() {
    this.showPassword.update((value) => !value);
  }

  // Nome e e-mail: erro aparece se o campo é inválido e o usuário já mexeu nele ou tentou enviar
  mostrarErro(campo: NgModel): boolean {
    return !!campo.invalid && (!!campo.touched || this.enviado());
  }

  // Senha: erro aparece assim que o usuário clica no campo, ou ao tentar enviar
  mostrarErroSenha(campo: NgModel): boolean {
    return !!campo.invalid && (this.senhaFocada() || this.enviado());
  }

  // A confirmação depende de outro campo, então a regra fica aqui
  senhasDiferentes(): boolean {
    return this.confirmarSenha !== this.senha;
  }

  // Confirmação: erro aparece assim que o usuário clica no campo, ou ao tentar enviar
  mostrarErroConfirmacao(campo: NgModel): boolean {
    const invalido = !!campo.invalid || this.senhasDiferentes();
    return invalido && (this.confirmarFocada() || this.enviado());
  }

  cadastrar(form: NgForm) {
    this.enviado.set(true);

    if (form.invalid || this.senhasDiferentes()) {
      return;
    }

    this.novoUsuario = {
      nome: this.nome,
      email: this.email,
      senha: this.senha,
    };

    this.usuarioService.cadastrarUsuario(this.novoUsuario)
      .subscribe({
        next: () => {
          this.sucesso.set(true);
          setTimeout(() => {
            this.router.navigate(['auth/login']);
          }, 2000); // espera 2s antes de redirecionar
        },
        error: (err) => console.log(err)
      });
  }
}