import { TipoMovimentacao } from './Movimentacao';

/** MovementCategoryPercentageDTOResponse do backend. */
export interface CategoriaAnalise {
  category: string;
  movementType: TipoMovimentacao;
  totalAmount: number;
  /** Percentual dentro do próprio tipo (entradas ou saídas), de 0 a 100. */
  percentage: number;
}

/** MovementCategoryAnalysisDTOResponse do backend. */
export interface AnaliseMensal {
  totalAmount: number;
  income: number;
  withdrawal: number;
  /** null quando o usuário ainda não criou a carteira. */
  currentBalance: number | null;
  analyzedMonth: string;
  analyzedYear: number;
  analysisDate: string;
  categoryBreakdown: CategoriaAnalise[];
}
