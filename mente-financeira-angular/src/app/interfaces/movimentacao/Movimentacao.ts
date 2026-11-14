export type TipoMovimentacao = 'ENTRADA' | 'RETIRADA';

/** MovementDTOResponse do backend. */
export interface Movimentacao {
  id: number;
  movedAmount: number;
  /** Formato dd/MM/yyyy HH:mm:ss */
  executionDate: string;
  movementType: TipoMovimentacao;
  movementCategory: string;
}

/** Só o que usamos da página do Spring (o restante do envelope varia entre versões). */
export interface Pagina<T> {
  content: T[];
}
