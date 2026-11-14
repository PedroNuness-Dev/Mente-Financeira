import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';

import { CadastroComponent, TEMPO_MINIMO_CELEBRACAO_MS } from './cadastro-component';
import { SplashService, TEMPO_COBRIR_MS } from '../../services/splash/splash-service';

const API = 'http://localhost:8080';

describe('CadastroComponent', () => {
  let component: CadastroComponent;
  let fixture: ComponentFixture<CadastroComponent>;
  let httpMock: HttpTestingController;
  let navigate: ReturnType<typeof vi.spyOn>;

  const preencher = () =>
    component.form.setValue({
      nome: 'Pedro Nunes',
      email: 'pedro@teste.com',
      senha: 'Senha123',
      confirmarSenha: 'Senha123',
    });

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CadastroComponent],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(CadastroComponent);
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

  it('não chama a API quando o formulário é inválido', () => {
    component.criarConta();

    httpMock.expectNone(`${API}/api/users`);
    expect(component.enviado()).toBe(true);
    expect(component.etapa()).toBe('formulario');
  });

  it('invalida o formulário quando as senhas são diferentes', () => {
    preencher();
    component.form.controls.confirmarSenha.setValue('Outra123');

    expect(component.form.hasError('senhasDiferentes')).toBe(true);
    expect(component.form.invalid).toBe(true);
  });

  it('atualiza a força da senha conforme os requisitos atendidos', () => {
    component.form.controls.senha.setValue('abc');
    expect(component.forcaSenha()).toBe(1);

    component.form.controls.senha.setValue('abc123');
    expect(component.forcaSenha()).toBe(2);

    component.form.controls.senha.setValue('abc12345');
    expect(component.forcaSenha()).toBe(3);
  });

  it('após cadastrar, faz login automático e entra no app pela splash de boas-vindas', async () => {
    vi.useFakeTimers();
    preencher();

    component.criarConta();
    expect(component.etapa()).toBe('enviando');

    const cadastro = httpMock.expectOne(`${API}/api/users`);
    expect(cadastro.request.body).toEqual({ name: 'Pedro Nunes', email: 'pedro@teste.com', password: 'Senha123' });
    cadastro.flush({ id: 1, name: 'Pedro Nunes', email: 'pedro@teste.com', firstLogin: true });

    expect(component.etapa()).toBe('concluido');
    expect(component.primeiroNome()).toBe('Pedro');

    const login = httpMock.expectOne(`${API}/api/auth/login`);
    expect(login.request.body).toEqual({ email: 'pedro@teste.com', password: 'Senha123' });
    login.flush({ token: 'jwt', expiresAt: '2999-01-01T00:00:00Z' });

    expect(navigate).not.toHaveBeenCalled();
    await vi.advanceTimersByTimeAsync(TEMPO_MINIMO_CELEBRACAO_MS);

    const splash = TestBed.inject(SplashService);
    expect(splash.ativa()).toBe(true);
    expect(splash.config()?.titulo).toBe('Tudo pronto, Pedro!');
    expect(navigate).not.toHaveBeenCalled();

    await vi.advanceTimersByTimeAsync(TEMPO_COBRIR_MS);
    expect(navigate).toHaveBeenCalledWith(['/home'], { replaceUrl: true });
    expect(localStorage.getItem('token')).toBe('jwt');
  });

  it('se o login automático falhar, leva ao login com o e-mail e um aviso', () => {
    vi.useFakeTimers();
    preencher();

    component.criarConta();
    httpMock.expectOne(`${API}/api/users`).flush({ id: 1, name: 'Pedro Nunes' });
    httpMock.expectOne(`${API}/api/auth/login`).flush(null, { status: 500, statusText: 'Erro' });
    vi.advanceTimersByTime(TEMPO_MINIMO_CELEBRACAO_MS);

    expect(navigate).toHaveBeenCalledWith(['/auth/login'], {
      state: expect.objectContaining({ email: 'pedro@teste.com', aviso: expect.any(String) }),
    });
  });

  it('mostra a mensagem de erro e volta ao formulário quando o cadastro falha', () => {
    preencher();

    component.criarConta();
    httpMock.expectOne(`${API}/api/users`).flush(null, { status: 400, statusText: 'Bad Request' });

    expect(component.etapa()).toBe('formulario');
    expect(component.erroCadastro()).toContain('e-mail pode já estar cadastrado');
    httpMock.expectNone(`${API}/api/auth/login`);
  });
});
