import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { OnboardingComponent, TEMPO_FECHAR_MS } from './onboarding-component';

const URL_CARTEIRA = 'http://localhost:8080/api/user/wallet';

describe('OnboardingComponent', () => {
  let component: OnboardingComponent;
  let fixture: ComponentFixture<OnboardingComponent>;
  let httpMock: HttpTestingController;

  const digitar = (texto: string) => {
    const campo = document.createElement('input');
    campo.value = texto;
    component.aoDigitarSaldo({ target: campo } as unknown as Event);
    return campo;
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OnboardingComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(OnboardingComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    await fixture.whenStable();
  });

  afterEach(() => {
    httpMock.verify();
    vi.useRealTimers();
  });

  it('navega entre os passos', () => {
    expect(component.passoAtual()).toBe(0);
    component.avancar();
    component.avancar();
    expect(component.ultimoPasso()).toBe(true);
    component.avancar();
    expect(component.passoAtual()).toBe(2);
    component.voltar();
    expect(component.passoAtual()).toBe(1);
  });

  it('o campo de saldo aceita só números e já mostra o valor formatado', () => {
    const campo = digitar('ab12x3,4-5');

    expect(component.centavos()).toBe(12345);
    expect(campo.value).toBe('123,45');
  });

  it('não envia saldo abaixo de R$ 1,00 e mostra o erro', () => {
    digitar('99');

    component.salvar();

    httpMock.expectNone(URL_CARTEIRA);
    expect(component.mostrarErroSaldo()).toBe(true);
  });

  it('cria a carteira com o saldo em reais e avisa quando concluir', async () => {
    vi.useFakeTimers();
    const concluido = vi.fn();
    component.concluido.subscribe(concluido);
    component.selecionarSugestao(1000);

    component.salvar();
    expect(component.enviando()).toBe(true);

    const req = httpMock.expectOne(URL_CARTEIRA);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ initialBalance: 1000 });
    req.flush({ id: 1, balance: 1000, movement: null });

    await vi.advanceTimersByTimeAsync(TEMPO_FECHAR_MS);
    expect(concluido).toHaveBeenCalled();
  });

  it('mostra a mensagem certa se a carteira já existir', () => {
    digitar('5000');

    component.salvar();
    httpMock.expectOne(URL_CARTEIRA).flush(null, { status: 409, statusText: 'Conflict' });

    expect(component.enviando()).toBe(false);
    expect(component.erro()).toContain('já foi configurada');
  });

  it('não fecha no meio do envio', () => {
    const fechado = vi.fn();
    component.fechado.subscribe(fechado);
    component.selecionarSugestao(500);
    component.salvar();

    component.fechar();

    expect(component.fechando()).toBe(false);
    httpMock.expectOne(URL_CARTEIRA).flush({ id: 1, balance: 500, movement: null });
  });
});
