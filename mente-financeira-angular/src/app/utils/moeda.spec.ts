import { centavosParaTexto, extrairCentavos, MAX_CENTAVOS } from './moeda';

describe('moeda', () => {
  it('extrairCentavos aproveita só os dígitos', () => {
    expect(extrairCentavos('1.234,56')).toBe(123456);
    expect(extrairCentavos('R$ -12a3')).toBe(123);
    expect(extrairCentavos('abc')).toBe(0);
    expect(extrairCentavos('')).toBe(0);
  });

  it('extrairCentavos ignora zeros à esquerda', () => {
    expect(extrairCentavos('0,05')).toBe(5);
  });

  it('extrairCentavos nunca passa do máximo aceito', () => {
    expect(extrairCentavos('9'.repeat(30))).toBe(MAX_CENTAVOS);
  });

  it('centavosParaTexto formata no padrão brasileiro', () => {
    expect(centavosParaTexto(0)).toBe('0,00');
    expect(centavosParaTexto(5)).toBe('0,05');
    expect(centavosParaTexto(123456)).toBe('1.234,56');
  });
});
