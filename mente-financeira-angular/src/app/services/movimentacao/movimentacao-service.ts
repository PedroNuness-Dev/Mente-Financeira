import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map } from 'rxjs';
import { API_URL } from '../../api';
import { AnaliseMensal } from '../../interfaces/movimentacao/AnaliseMensal';
import { Movimentacao, Pagina } from '../../interfaces/movimentacao/Movimentacao';
import { formatarDataIso } from '../../utils/formatar-data';

@Injectable({
  providedIn: 'root',
})
export class MovimentacaoService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_URL}/api/user/wallet/movements`;

  /**
   * Resumo de um período: entradas, saídas, saldo atual e totais por categoria.
   * As duas datas são inclusivas (o backend busca até 23:59:59 do dia final).
   */
  analisarPeriodo(inicio: Date, fim: Date) {
    return this.http.get<AnaliseMensal>(`${this.url}/analysis`, {
      params: { initialDate: formatarDataIso(inicio), finalDate: formatarDataIso(fim) },
    });
  }

  /** Resumo do mês inteiro (mes de 1 a 12), do dia 1 ao último dia. */
  analisarMes(mes: number, ano: number) {
    // Dia 0 do mês seguinte = último dia deste mês
    return this.analisarPeriodo(new Date(ano, mes - 1, 1), new Date(ano, mes, 0));
  }

  /** As movimentações mais recentes (o backend ordena da mais nova para a mais antiga). */
  buscarUltimas(quantidade: number) {
    return this.http
      .get<Pagina<Movimentacao>>(this.url, { params: { page: 0, size: quantidade } })
      .pipe(map((pagina) => pagina.content ?? []));
  }
}
