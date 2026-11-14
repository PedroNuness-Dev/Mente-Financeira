/** Maior valor aceito nos campos de dinheiro: R$ 999.999.999,99. */
export const MAX_CENTAVOS = 99_999_999_999;

/**
 * Lê só os dígitos de um texto e devolve o valor em centavos.
 * Qualquer outro caractere (letras, sinais, espaços) é descartado, então o
 * resultado é sempre um inteiro não negativo e limitado ao máximo aceito.
 */
export function extrairCentavos(texto: string): number {
  const digitos = texto.replace(/\D/g, '').replace(/^0+/, '').slice(0, String(MAX_CENTAVOS).length);
  return Math.min(Number(digitos || 0), MAX_CENTAVOS);
}

/** 123456 -> "1.234,56" (sem o símbolo R$). */
export function centavosParaTexto(centavos: number): string {
  return (centavos / 100).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}
