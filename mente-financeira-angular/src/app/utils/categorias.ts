/** Nome de exibição das categorias do backend (enum MovementCategory). */
const ROTULOS: Record<string, string> = {
  ALIMENTACAO: 'Alimentação',
  MERCADO: 'Mercado',
  TRANSPORTE: 'Transporte',
  MORADIA: 'Moradia',
  CONTAS_FIXAS: 'Contas fixas',
  SAUDE: 'Saúde',
  EDUCACAO: 'Educação',
  LAZER: 'Lazer',
  VESTUARIO: 'Vestuário',
  ASSINATURAS: 'Assinaturas',
  VIAGEM: 'Viagem',
  PET: 'Pet',
  PRESENTES: 'Presentes',
  INVESTIMENTOS: 'Investimentos',
  SALARIO: 'Salário',
  RENDIMENTOS: 'Rendimentos',
  TRANSFERENCIA: 'Transferência',
  DEPOSITO: 'Depósito',
  OUTROS: 'Outros',
};

/** Categoria que o front ainda não conhece vira texto legível: CONTAS_NOVAS -> "Contas novas". */
export function rotuloCategoria(categoria: string): string {
  if (ROTULOS[categoria]) return ROTULOS[categoria];
  const texto = categoria.replace(/_/g, ' ').toLowerCase();
  return texto.charAt(0).toUpperCase() + texto.slice(1);
}
