export function formatarDataBr(data: string | undefined): string {
  if (!data) return '';

  const [dataParte] = data.split(' ');
  const [dia, mes, ano] = dataParte.split('/');

  const meses = [
    'janeiro', 'fevereiro', 'março', 'abril', 'maio', 'junho',
    'julho', 'agosto', 'setembro', 'outubro', 'novembro', 'dezembro',
  ];

  return `${dia} de ${meses[Number(mes) - 1]} de ${ano}`;
}

/** "Sábado, 14 de novembro de 2026" */
export function formatarDataPorExtenso(data: Date): string {
  const texto = data.toLocaleDateString('pt-BR', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });
  return texto.charAt(0).toUpperCase() + texto.slice(1);
}

/** "14/11/2026 13:20:05" (formato do backend) -> "14/11 às 13:20" */
export function formatarDataCurta(data: string | undefined): string {
  if (!data) return '';
  const [dia, hora = ''] = data.split(' ');
  const [d, m] = dia.split('/');
  const [h, min] = hora.split(':');
  return h && min ? `${d}/${m} às ${h}:${min}` : `${d}/${m}`;
}