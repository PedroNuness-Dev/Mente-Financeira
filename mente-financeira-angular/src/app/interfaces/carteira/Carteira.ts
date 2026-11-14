import { Movimentacao } from '../movimentacao/Movimentacao';

/** WalletRequestDTO do backend. */
export interface CarteiraRequest {
  initialBalance: number;
}

/** WalletResponseDTO do backend. */
export interface CarteiraResponse {
  id: number;
  balance: number;
  movement: Movimentacao;
}

/** WalletCheckRequest do backend. */
export interface CarteiraCheck {
  haveWallet: boolean;
}
