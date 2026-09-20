import { Component, signal } from '@angular/core';
import { FormsModule, NgForm, NgModel } from '@angular/forms';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-login-component',
  imports: [RouterLink,FormsModule],
  templateUrl: './login-component.html',
  styleUrl: './login-component.scss',
})
export class LoginComponent {

  email = '';
  senha = '';
  showPassword = signal(false);
  enviado = signal(false);

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

    const credenciais = {
      email: this.email,
      senha: this.senha,
    };

    console.log('Enviando login para o backend:', credenciais);
  }
}
