import { Component, inject, signal } from '@angular/core';
import { FormsModule, NgForm, NgModel } from '@angular/forms';
import { RouterLink } from '@angular/router';
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

  email = '';
  senha = '';
  showPassword = signal(false);
  enviado = signal(false);
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

    if (form.invalid) {
      return;
    }

    this.loginRequest = {
      email: this.email,
      senha: this.senha,
    };

    this.authService.login(this.loginRequest)
    .subscribe({
      next: () => {console.log("Login efetuado com sucesso!")},
      error: (err) => {console.log(err)}
    })
  }
}
