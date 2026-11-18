const MESES = [
  'janeiro', 'fevereiro', 'março', 'abril', 'maio', 'junho',
  'julho', 'agosto', 'setembro', 'outubro', 'novembro', 'dezembro',
];

const ISO = /^(\d{4})-(\d{2})-(\d{2})(?:[T ](\d{2}):(\d{2})(?::(\d{2}))?)?/;
const BR = /^(\d{2})\/(\d{2})\/(\d{4})(?:\s+(\d{2}):(\d{2})(?::(\d{2}))?)?/;

const doisDigitos = (n: number) => String(n).padStart(2, '0');

/**
 * Lê as datas que vêm do backend como horário local.
 * Aceita o ISO do Jackson ("2026-11-14T13:20:05", com ou sem fração de segundo)
 * e o formato antigo "14/11/2026 13:20:05". null se não der para ler.
 *
 * O parse é manual porque o `new Date()` não entende o formato BR e trata
 * frações com mais de 3 casas de um jeito diferente em cada navegador.
 */
export function lerDataHora(data: string | null | undefined): Date | null {
  if (!data) return null;

  let partes: number[];
  const iso = ISO.exec(data);
  const br = iso ? null : BR.exec(data);

  if (iso) {
    const [, ano, mes, dia, h = '0', min = '0', s = '0'] = iso;
    partes = [ano, mes, dia, h, min, s].map(Number);
  } else if (br) {
    const [, dia, mes, ano, h = '0', min = '0', s = '0'] = br;
    partes = [ano, mes, dia, h, min, s].map(Number);
  } else {
    return null;
  }

  const [ano, mes, dia, h, min, s] = partes;
  const resultado = new Date(ano, mes - 1, dia, h, min, s);
  // Descarta datas impossíveis (31/02 viraria 03/03 sem esse cuidado)
  return resultado.getMonth() === mes - 1 && resultado.getDate() === dia ? resultado : null;
}

/** Date -> "2026-11-14" no fuso local (o toISOString() converteria para UTC e poderia mudar o dia). */
export function formatarDataIso(data: Date): string {
  return `${data.getFullYear()}-${doisDigitos(data.getMonth() + 1)}-${doisDigitos(data.getDate())}`;
}

/** "14 de novembro de 2026" */
export function formatarDataBr(data: string | null | undefined): string {
  const lida = lerDataHora(data);
  if (!lida) return '';
  return `${doisDigitos(lida.getDate())} de ${MESES[lida.getMonth()]} de ${lida.getFullYear()}`;
}

/** "Sábado, 14 de novembro de 2026" */
export function formatarDataPorExtenso(data: Date): string {
  const texto = data.toLocaleDateString('pt-BR', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });
  return texto.charAt(0).toUpperCase() + texto.slice(1);
}

/** "2026-11-14T13:20:05" -> "14/11 às 13:20" */
export function formatarDataCurta(data: string | null | undefined): string {
  const lida = lerDataHora(data);
  if (!lida) return '';
  const diaMes = `${doisDigitos(lida.getDate())}/${doisDigitos(lida.getMonth() + 1)}`;
  return `${diaMes} às ${doisDigitos(lida.getHours())}:${doisDigitos(lida.getMinutes())}`;
}
