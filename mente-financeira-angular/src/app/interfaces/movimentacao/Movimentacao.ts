export type TipoMovimentacao = 'ENTRADA' | 'RETIRADA';

/** MovementResponse do backend. */
export interface Movimentacao {
  movementId: number;
  movedAmount: number;
  /** ISO local, por exemplo "2026-11-14T13:20:05.123456" (ler com `lerDataHora`). */
  executionDate: string;
  movementType: TipoMovimentacao;
  movementCategory: string;
}

/** Só o que usamos da página do Spring (o restante do envelope varia entre versões). */
export interface Pagina<T> {
  content: T[];
}
