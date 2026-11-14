import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';

import { LoginComponent } from './login-component';
import { SplashService, TEMPO_COBRIR_MS } from '../../services/splash/splash-service';

const URL_LOGIN = 'http://localhost:8080/api/auth/login';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let httpMock: HttpTestingController;
  let navigate: ReturnType<typeof vi.spyOn>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    await fixture.whenStable();
  });

  afterEach(() => {
    httpMock.verify();
    TestBed.inject(SplashService).encerrar();
    vi.useRealTimers();
    localStorage.clear();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('não mostra erro de campo obrigatório só por entrar e sair do campo', () => {
    component.form.controls.email.markAsTouched();
    component.form.controls.senha.markAsTouched();

    expect(component.mostrarErro('email')).toBe(false);
    expect(component.mostrarErro('senha')).toBe(false);
  });

  it('não chama a API quando o formulário é inválido, e aí sim mostra os erros', () => {
    component.entrar();

    httpMock.expectNone(URL_LOGIN);
    expect(component.mostrarErro('email')).toBe(true);
    expect(component.mostrarErro('senha')).toBe(true);
  });

  it('faz login, mostra a splash com o primeiro nome e só então navega para a home', async () => {
    vi.useFakeTimers();
    component.form.setValue({ email: 'pedro@teste.com', senha: 'Senha123' });

    component.entrar();
    expect(component.carregando()).toBe(true);

    const req = httpMock.expectOne(URL_LOGIN);
    expect(req.request.body).toEqual({ email: 'pedro@teste.com', password: 'Senha123' });
    req.flush({ token: 'jwt', expiresAt: '2999-01-01T00:00:00Z', name: 'Pedro Nunes' });

    const splash = TestBed.inject(SplashService);
    expect(splash.ativa()).toBe(true);
    expect(splash.config()?.titulo).toBe('Bem-vindo(a) de volta, Pedro!');
    // A rota só troca quando as ondas de cor já cobriram a tela
    expect(navigate).not.toHaveBeenCalled();

    await vi.advanceTimersByTimeAsync(TEMPO_COBRIR_MS);
    expect(navigate).toHaveBeenCalledWith(['/home'], { replaceUrl: true });
  });

  it('mostra "E-mail ou senha inválidos" quando a API responde 401', () => {
    component.form.setValue({ email: 'pedro@teste.com', senha: 'errada1' });

    component.entrar();
    httpMock.expectOne(URL_LOGIN).flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(component.erroLogin()).toBe('E-mail ou senha inválidos.');
    expect(component.carregando()).toBe(false);
    expect(navigate).not.toHaveBeenCalled();
  });

  it('mostra mensagem de conexão quando o servidor não responde', () => {
    component.form.setValue({ email: 'pedro@teste.com', senha: 'Senha123' });

    component.entrar();
    httpMock.expectOne(URL_LOGIN).error(new ProgressEvent('error'), { status: 0 });

    expect(component.erroLogin()).toContain('servidor');
  });
});
