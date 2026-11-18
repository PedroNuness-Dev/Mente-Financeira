import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { MovimentacaoService } from './movimentacao-service';

const API = 'http://localhost:8080/api/user/wallet/movements';

describe('MovimentacaoService', () => {
  let service: MovimentacaoService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(MovimentacaoService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('analisa o período mandando as duas datas em ISO', () => {
    service.analisarPeriodo(new Date(2026, 10, 3), new Date(2026, 10, 9)).subscribe();

    const req = httpMock.expectOne((r) => r.url === `${API}/analysis`);
    expect(req.request.params.get('initialDate')).toBe('2026-11-03');
    expect(req.request.params.get('finalDate')).toBe('2026-11-09');
    req.flush({});
  });

  it('analisa o mês inteiro, do dia 1 ao último dia', () => {
    service.analisarMes(2, 2028).subscribe(); // 2028 é bissexto

    const req = httpMock.expectOne((r) => r.url === `${API}/analysis`);
    expect(req.request.params.get('initialDate')).toBe('2028-02-01');
    expect(req.request.params.get('finalDate')).toBe('2028-02-29');
    req.flush({});
  });

  it('dezembro termina no dia 31 do mesmo ano', () => {
    service.analisarMes(12, 2026).subscribe();

    const req = httpMock.expectOne((r) => r.url === `${API}/analysis`);
    expect(req.request.params.get('initialDate')).toBe('2026-12-01');
    expect(req.request.params.get('finalDate')).toBe('2026-12-31');
    req.flush({});
  });

  it('busca as últimas movimentações sem filtro de período', () => {
    let resultado: unknown[] | undefined;
    service.buscarUltimas(5).subscribe((lista) => (resultado = lista));

    const req = httpMock.expectOne((r) => r.url === API);
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('size')).toBe('5');
    expect(req.request.params.has('initialDate')).toBe(false);
    req.flush({ content: [{ movementId: 1 }], page: { size: 5, number: 0, totalElements: 1, totalPages: 1 } });

    expect(resultado).toEqual([{ movementId: 1 }]);
  });
});
