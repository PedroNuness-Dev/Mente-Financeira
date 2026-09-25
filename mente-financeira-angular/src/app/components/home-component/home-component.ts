import { Component, inject, signal } from '@angular/core';
import { UsuarioService } from '../../services/usuario/usuario-service';
import { formatarDataBr } from '../../utils/formatar-data';
import { OnboardingComponent } from '../onboarding-component/onboarding-component';

@Component({
  selector: 'app-home-component',
  imports: [OnboardingComponent],
  templateUrl: './home-component.html',
  styleUrl: './home-component.scss',
})
export class HomeComponent {
  private usuarioService = inject(UsuarioService);

  usuario = this.usuarioService.usuario;
  mostrarOnboarding = signal(false);

  get primeiroNome(): string {
    return this.usuario()?.name.split(' ')[0] ?? '';
  }

  get saudacao(): string {
    const hora = new Date().getHours();
    if (hora < 12) return 'Bom dia';
    if (hora < 18) return 'Boa tarde';
    return 'Boa noite';
  }

  get dataFormatada(): string {
    return formatarDataBr(this.usuario()?.creationDate);
  }

  finalizarOnboarding(saldoInicial: number): void {
    console.log('Onboarding concluído, saldo inicial informado:', saldoInicial);
    this.mostrarOnboarding.set(false);
  }
}