import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map } from 'rxjs';
import { API_URL } from '../../api';
import { AnaliseMensal } from '../../interfaces/movimentacao/AnaliseMensal';
import { Movimentacao, Pagina } from '../../interfaces/movimentacao/Movimentacao';

@Injectable({
  providedIn: 'root',
})
export class MovimentacaoService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_URL}/api/user/wallet/movements`;

  /** Resumo do mês: entradas, saídas, saldo atual e totais por categoria. */
  analisarMes(mes: number, ano: number) {
    return this.http.get<AnaliseMensal>(`${this.url}/${mes}/${ano}`);
  }

  /** As movimentações mais recentes (o backend já ordena da mais nova para a mais antiga). */
  buscarUltimas(quantidade: number) {
    return this.http
      .get<Pagina<Movimentacao>>(this.url, { params: { page: 0, size: quantidade } })
      .pipe(map((pagina) => pagina.content ?? []));
  }
}
