import { Component, inject, signal } from '@angular/core';
import { FormsModule, NgForm, NgModel } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth/auth-service';
import { LoginRequest } from '../../interfaces/auth/LoginRequest';

@Component({
  selector: 'app-login-component',
  imports: [RouterLink,FormsModule],
  templateUrl: './login-component.html',
  styleUrl: './login-component.scss',
})
export class LoginComponent {

  authService = inject(AuthService);
  router = inject(Router);

  email = '';
  senha = '';
  showPassword = signal(false);
  enviado = signal(false);
  erroLogin = signal<string | null>(null);
  loginRequest : LoginRequest | null = null;

  togglePassword() {
    this.showPassword.update((value) => !value);
  }

  // Erro aparece se o campo é inválido e o usuário já mexeu nele ou tentou enviar
  mostrarErro(campo: NgModel): boolean {
    return !!campo.invalid && this.enviado();
  }

  login(form: NgForm) {
    this.enviado.set(true);
    this.erroLogin.set(null);

    if (form.invalid) {
      return;
    }

    this.loginRequest = {
      email: this.email,
      password: this.senha,
    };

    this.authService.login(this.loginRequest)
    .subscribe({
      next: () => {
        console.log("Login efetuado com sucesso!");
        this.router.navigate(["/home"])
      },
      error: (err) => {
        console.log(err);
        this.erroLogin.set('E-mail ou senha inválidos.');
      }
    })
  }
}
