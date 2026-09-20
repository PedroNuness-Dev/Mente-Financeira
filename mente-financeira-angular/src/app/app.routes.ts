import { Routes } from '@angular/router';
import { AuthComponent } from './components/auth-component/auth-component';
import { LoginComponent } from './components/login-component/login-component';
import { CadastroComponent } from './components/cadastro-component/cadastro-component';

export const routes: Routes = [
  { path: '', redirectTo: 'auth/login', pathMatch: 'full' },
  {
    path: 'auth',
    component: AuthComponent,
    children: [
      { path: '', redirectTo: 'login', pathMatch: 'full' },
      { path: 'login', component: LoginComponent },
      { path: 'cadastro', component: CadastroComponent },
    ],
  },
  { path: '**', redirectTo: 'auth/login' },
];
