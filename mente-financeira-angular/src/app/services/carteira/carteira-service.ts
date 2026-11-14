import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { API_URL } from '../../api';
import { CarteiraCheck, CarteiraRequest, CarteiraResponse } from '../../interfaces/carteira/Carteira';

@Injectable({
  providedIn: 'root',
})
export class CarteiraService {
  private readonly http = inject(HttpClient);

  /** Cria a carteira do usuário autenticado com o saldo inicial (em reais). */
  criarCarteira(saldoInicial: number) {
    const corpo: CarteiraRequest = { initialBalance: saldoInicial };
    return this.http.post<CarteiraResponse>(`${API_URL}/api/user/wallet`, corpo);
  }

  /** Diz se o usuário autenticado já tem carteira (GET /api/user/wallet/check). */
  verificarCarteira() {
    return this.http.get<CarteiraCheck>(`${API_URL}/api/user/wallet/check`);
  }
}
