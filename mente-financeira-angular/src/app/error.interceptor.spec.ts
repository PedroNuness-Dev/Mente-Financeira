import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';

import { errorInterceptor } from './error.interceptor';
import { AuthService } from './services/auth/auth-service';

describe('errorInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let navigate: ReturnType<typeof vi.spyOn>;
  let logout: ReturnType<typeof vi.spyOn>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    logout = vi.spyOn(TestBed.inject(AuthService), 'logout');
  });

  afterEach(() => httpMock.verify());

  it('com 401 numa rota protegida, faz logout e volta para o login', () => {
    http.get('/api/users/me').subscribe({ error: () => {} });
    httpMock.expectOne('/api/users/me').flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(logout).toHaveBeenCalled();
    expect(navigate).toHaveBeenCalledWith(['/auth/login'], { replaceUrl: true });
  });

  it('com 401 no próprio login, deixa a tela tratar o erro', () => {
    http.post('/api/auth/login', {}).subscribe({ error: () => {} });
    httpMock.expectOne('/api/auth/login').flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(logout).not.toHaveBeenCalled();
    expect(navigate).not.toHaveBeenCalled();
  });
});
