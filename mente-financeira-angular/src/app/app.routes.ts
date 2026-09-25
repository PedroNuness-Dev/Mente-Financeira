import { Routes } from '@angular/router';
import { AuthComponent } from './components/auth-component/auth-component';
import { LoginComponent } from './components/login-component/login-component';
import { CadastroComponent } from './components/cadastro-component/cadastro-component';
import { LayoutComponent } from './components/layout-component/layout-component';
import { HomeComponent } from './components/home-component/home-component';
import { authGuard } from './auth.guard';
import { ConfiguracoesComponent } from './components/configuracoes-component/configuracoes-component';
import { RelatoriosComponent } from './components/relatorios-component/relatorios-component';
import { MovimentacoesComponent } from './components/movimentacoes-component/movimentacoes-component';
import { CategoriasComponent } from './components/categorias-component/categorias-component';

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
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: 'home', component: HomeComponent },
      { path: 'movimentacoes', component: MovimentacoesComponent },
      { path: 'categorias', component: CategoriasComponent },
      { path: 'relatorios', component: RelatoriosComponent },
      { path: 'configuracoes', component: ConfiguracoesComponent },
    ]
  },
  { path: '**', redirectTo: 'auth/login' },
];