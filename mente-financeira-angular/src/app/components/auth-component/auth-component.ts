import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AuthHeroComponent } from '../auth-hero-component/auth-hero-component';

@Component({
  selector: 'app-auth-component',
  imports: [RouterOutlet, AuthHeroComponent],
  templateUrl: './auth-component.html',
  styleUrl: './auth-component.scss',
})
export class AuthComponent {
  protected readonly anoAtual = new Date().getFullYear();
}
