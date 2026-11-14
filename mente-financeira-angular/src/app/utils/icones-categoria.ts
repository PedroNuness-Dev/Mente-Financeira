/**
 * Ícone de cada categoria (enum MovementCategory), como lista de traços SVG
 * num viewBox 24x24, desenhados com stroke. Ficam como dados (e não como HTML)
 * para o template montar o <svg> sem precisar de innerHTML.
 */
const ICONES: Record<string, string[]> = {
  ALIMENTACAO: ['M4 3v8a2 2 0 0 0 2 2h1v8', 'M7 3v6', 'M10 3v8a2 2 0 0 1-2 2', 'M17 21V3c-2.2 1-3.5 3.5-3.5 7v3H17'],
  MERCADO: ['M3 4h2l2.4 11h10.2L20 8H6.2', 'M9 20h.01', 'M17 20h.01'],
  TRANSPORTE: ['M5 17h14v-5l-2-5H7l-2 5v5z', 'M5 12h14', 'M7.5 17v2', 'M16.5 17v2', 'M8 14.5h.01', 'M16 14.5h.01'],
  MORADIA: ['M3 11 12 4l9 7', 'M5 10v10h14V10', 'M10 20v-6h4v6'],
  CONTAS_FIXAS: ['M6 3h12v18l-3-2-3 2-3-2-3 2V3z', 'M9 8h6', 'M9 12h6'],
  SAUDE: ['M12 20s-7-4.4-7-10a4 4 0 0 1 7-2.6A4 4 0 0 1 19 10c0 5.6-7 10-7 10z', 'M9 11h6', 'M12 8v6'],
  EDUCACAO: ['M2 9l10-5 10 5-10 5-10-5z', 'M6 11v5c0 1.5 2.7 3 6 3s6-1.5 6-3v-5', 'M22 9v5'],
  LAZER: ['M6 9h12a3 3 0 0 1 3 3v2a3 3 0 0 1-5.2 2H8.2A3 3 0 0 1 3 14v-2a3 3 0 0 1 3-3z', 'M7 11v3', 'M5.5 12.5h3', 'M16 12h.01', 'M18 14h.01'],
  VESTUARIO: ['M9 3 4 6l2 4 2-1v12h8V9l2 1 2-4-5-3a3 3 0 0 1-6 0z'],
  ASSINATURAS: ['M20 11a8 8 0 1 0-2.3 5.7', 'M20 5v6h-6'],
  VIAGEM: ['M10.5 13.5 3 11l1.5-1.5 7 1L16 6a2.1 2.1 0 0 1 3 3l-4.5 4.5 1 7L14 22l-2.5-7.5', 'M6 18l-3 3'],
  PET: ['M8 8.5a1.5 1.5 0 1 0 0-3 1.5 1.5 0 0 0 0 3z', 'M16 8.5a1.5 1.5 0 1 0 0-3 1.5 1.5 0 0 0 0 3z', 'M4.5 12.5a1.5 1.5 0 1 0 0-3 1.5 1.5 0 0 0 0 3z', 'M19.5 12.5a1.5 1.5 0 1 0 0-3 1.5 1.5 0 0 0 0 3z', 'M12 12c-2.5 0-5 3.5-5 6a2 2 0 0 0 2.5 2c1-.3 1.7-.5 2.5-.5s1.5.2 2.5.5A2 2 0 0 0 17 18c0-2.5-2.5-6-5-6z'],
  PRESENTES: ['M4 11h16v10H4z', 'M3 7h18v4H3z', 'M12 7v14', 'M12 7c-1.5-3-5-3.5-5-1.5S10 7 12 7z', 'M12 7c1.5-3 5-3.5 5-1.5S14 7 12 7z'],
  INVESTIMENTOS: ['M3 17l6-6 4 4 8-8', 'M15 7h6v6'],
  SALARIO: ['M3 7h18v12H3z', 'M8 7V5h8v2', 'M3 12h18', 'M12 11v2'],
  RENDIMENTOS: ['M12 3v18', 'M17 7.5c0-1.9-2.2-3-5-3s-5 1.1-5 3 2.2 2.7 5 3.5 5 1.6 5 3.5-2.2 3-5 3-5-1.1-5-3'],
  TRANSFERENCIA: ['M4 8h14', 'M14 4l4 4-4 4', 'M20 16H6', 'M10 12l-4 4 4 4'],
  DEPOSITO: ['M4 10h16', 'M12 3l8 5H4l8-5z', 'M6 10v8', 'M10 10v8', 'M14 10v8', 'M18 10v8', 'M3 21h18'],
  OUTROS: ['M5 12h.01', 'M12 12h.01', 'M19 12h.01'],
};

/** Categoria sem ícone próprio usa o de "Outros". */
export function iconeCategoria(categoria: string): string[] {
  return ICONES[categoria] ?? ICONES['OUTROS'];
}
