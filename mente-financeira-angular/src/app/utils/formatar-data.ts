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