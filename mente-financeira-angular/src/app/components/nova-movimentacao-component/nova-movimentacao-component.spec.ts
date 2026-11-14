import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { vi } from 'vitest';
import { NovaMovimentacaoComponent } from './nova-movimentacao-component';

const API = 'http://localhost:8080/api/user/wallet';

describe('NovaMovimentacaoComponent', () => {
  let fixture: ComponentFixture<NovaMovimentacaoComponent>;
  let component: NovaMovimentacaoComponent;
  let httpMock: HttpTestingController;

  async function criar(tipo: 'ENTRADA' | 'RETIRADA', saldo: number | null = null) {
    fixture = TestBed.createComponent(NovaMovimentacaoComponent);
    fixture.componentRef.setInput('tipo', tipo);
    fixture.componentRef.setInput('saldoAtual', saldo);
    component = fixture.componentInstance;
    await fixture.whenStable();
  }

  function preencher(centavos: number, categoria: string, descricao: string) {
    component.centavos.set(centavos);
    component.form.setValue({ categoria, descricao });
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [NovaMovimentacaoComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    vi.useRealTimers();
    httpMock.verify();
  });

  it('abre fixo no tipo informado', async () => {
    await criar('ENTRADA');
    expect(component.entrada()).toBe(true);
  });

  it('envia o depósito com o valor em reais e a descrição sem espaços nas pontas', async () => {
    await criar('ENTRADA');
    preencher(150050, 'SALARIO', '  salário  ');
    component.salvar();

    const req = httpMock.expectOne(`${API}/deposit`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ depositAmount: 1500.5, movementCategory: 'SALARIO', description: 'salário' });
  });

  it('envia a retirada para /withdraw e avisa o pai ao concluir', async () => {
    await criar('RETIRADA', 1000);
    vi.useFakeTimers();
    const concluido = vi.fn();
    component.concluido.subscribe(concluido);
    preencher(2000, 'MERCADO', 'feira');
    component.salvar();

    const req = httpMock.expectOne(`${API}/withdraw`);
    expect(req.request.body).toEqual({ withdrawalAmount: 20, movementCategory: 'MERCADO', description: 'feira' });
    req.flush({ id: 1, balance: 980, movement: null });
    vi.runAllTimers();
    expect(concluido).toHaveBeenCalled();
  });

  it('não envia com valor abaixo de R$ 1,00 ou sem descrição', async () => {
    await criar('RETIRADA');
    preencher(50, 'MERCADO', 'feira');
    component.salvar();
    preencher(500, 'MERCADO', '   ');
    component.salvar();

    httpMock.expectNone(() => true);
    expect(component.mostrarErroValor()).toBe(false);
    expect(component.campoInvalido('descricao')).toBe(true);
  });

  it('avisa quando a retirada deixaria o saldo negativo', async () => {
    await criar('RETIRADA', 10);
    component.centavos.set(1500);
    expect(component.saldoNegativo()).toContain('5,00');
  });

  it('filtra as categorias pelo nome, ignorando acentos e maiúsculas', async () => {
    await criar('RETIRADA');
    component.busca.set('SAUDE');
    expect(component.categoriasFiltradas().map((c) => c.valor)).toEqual(['SAUDE']);
    component.busca.set('xyz');
    expect(component.categoriasFiltradas()).toEqual([]);
  });

  it('oferece só as categorias do próprio tipo', async () => {
    await criar('ENTRADA');
    const valores = component.categorias().map((c) => c.valor);
    expect(valores).toContain('SALARIO');
    expect(valores).not.toContain('MERCADO');
  });

  it('mostra a mensagem de erro quando o backend recusa', async () => {
    await criar('ENTRADA');
    preencher(1000, 'SALARIO', 'salário');
    component.salvar();
    httpMock.expectOne(`${API}/deposit`).flush(null, { status: 404, statusText: 'Not Found' });

    expect(component.erro()).toContain('carteira');
    expect(component.form.enabled).toBe(true);
  });
});
