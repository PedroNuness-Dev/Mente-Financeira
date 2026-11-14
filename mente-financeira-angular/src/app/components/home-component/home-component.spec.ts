import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { HomeComponent } from './home-component';
import { AnaliseMensal } from '../../interfaces/movimentacao/AnaliseMensal';

const API = 'http://localhost:8080/api/user/wallet/movements';
const URL_CHECK = 'http://localhost:8080/api/user/wallet/check';

describe('HomeComponent', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;
  let httpMock: HttpTestingController;
  const hoje = new Date();
  const urlAnalise = `${API}/${hoje.getMonth() + 1}/${hoje.getFullYear()}`;

  const analise = (parcial: Partial<AnaliseMensal>): AnaliseMensal => ({
    totalAmount: 0,
    income: 0,
    withdrawal: 0,
    currentBalance: 0,
    analyzedMonth: 'novembro',
    analyzedYear: hoje.getFullYear(),
    analysisDate: '',
    categoryBreakdown: [],
    ...parcial,
  });

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HomeComponent],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(HomeComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => httpMock.verify());

  const responderCheck = (haveWallet: boolean) => httpMock.expectOne(URL_CHECK).flush({ haveWallet });

  it('busca o resumo do mês atual e calcula o resultado (entradas − saídas)', async () => {
    expect(component.estado()).toBe('carregando');

    responderCheck(true);
    httpMock.expectOne(urlAnalise).flush(analise({ income: 3000, withdrawal: 1200, currentBalance: 5800 }));
    httpMock.expectOne((req) => req.url === API && req.params.get('size') === '5').flush({ content: [] });
    await fixture.whenStable();

    expect(component.temCarteira()).toBe(true);
    expect(component.saldo()).toBe(5800);
    expect(component.resultado()).toBe(1800);
    expect(component.situacao().rotulo).toBe('Positivo');
    expect(component.percentualGasto()).toBe(40);
  });

  it('ordena as saídas por categoria, da maior para a menor, ignorando entradas', async () => {
    responderCheck(true);
    httpMock.expectOne(urlAnalise).flush(
      analise({
        income: 1000,
        withdrawal: 300,
        currentBalance: 700,
        categoryBreakdown: [
          { category: 'LAZER', movementType: 'RETIRADA', totalAmount: 100, percentage: 33.33 },
          { category: 'SALARIO', movementType: 'ENTRADA', totalAmount: 1000, percentage: 100 },
          { category: 'MERCADO', movementType: 'RETIRADA', totalAmount: 200, percentage: 66.67 },
        ],
      }),
    );
    httpMock.expectOne((req) => req.url === API).flush({ content: [] });
    await fixture.whenStable();

    expect(component.gastosPorCategoria().map((c) => c.nome)).toEqual(['Mercado', 'Lazer']);
    expect(component.gastosPorCategoria()[0].percentual).toBe(67);
  });

  it('sem entradas no mês, não calcula percentual gasto', async () => {
    responderCheck(true);
    httpMock.expectOne(urlAnalise).flush(analise({ withdrawal: 50, currentBalance: 100 }));
    httpMock.expectOne((req) => req.url === API).flush({ content: [] });
    await fixture.whenStable();

    expect(component.percentualGasto()).toBeNull();
    expect(component.situacao().rotulo).toBe('Negativo');
  });

  it('sem carteira, não busca movimentações e abre a configuração inicial', async () => {
    responderCheck(false);
    httpMock.expectOne(urlAnalise).flush(analise({ currentBalance: null }));
    await fixture.whenStable();

    httpMock.expectNone((req) => req.url === API);
    expect(component.temCarteira()).toBe(false);
    expect(component.mostrarOnboarding()).toBe(true);
  });

  it('se as últimas movimentações falharem, o resto do painel continua de pé', async () => {
    responderCheck(true);
    httpMock.expectOne(urlAnalise).flush(analise({ currentBalance: 10 }));
    httpMock.expectOne((req) => req.url === API).flush(null, { status: 500, statusText: 'Erro' });
    await fixture.whenStable();

    expect(component.estado()).toBe('pronto');
    expect(component.ultimas()).toBeNull();
  });

  it('mostra o estado de erro quando o resumo falha', async () => {
    responderCheck(true);
    httpMock.expectOne(urlAnalise).flush(null, { status: 500, statusText: 'Erro' });
    await fixture.whenStable();

    expect(component.estado()).toBe('erro');
  });
});
