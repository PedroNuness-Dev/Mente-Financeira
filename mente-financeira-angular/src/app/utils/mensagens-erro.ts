import { HttpErrorResponse } from '@angular/common/http';

/*
 * Mensagens exibidas ao usuário a partir do status HTTP.
 * Propositalmente NÃO usam o corpo de erro do backend (campo "messagem"),
 * que ainda está com nome provisório e traz textos técnicos em inglês.
 */

const SEM_CONEXAO = 'Não conseguimos falar com o servidor. Verifique sua conexão e tente novamente.';

export function mensagemErroLogin(erro: HttpErrorResponse): string {
  if (erro.status === 0) return SEM_CONEXAO;
  if (erro.status === 400 || erro.status === 401) return 'E-mail ou senha inválidos.';
  return 'Algo deu errado ao entrar. Tente novamente em instantes.';
}

export function mensagemErroCadastro(erro: HttpErrorResponse): string {
  if (erro.status === 0) return SEM_CONEXAO;
  if (erro.status === 400 || erro.status === 409) {
    return 'Não foi possível criar sua conta. Confira os dados — este e-mail pode já estar cadastrado.';
  }
  return 'Algo deu errado ao criar sua conta. Tente novamente em instantes.';
}

export function mensagemErroCarteira(erro: HttpErrorResponse): string {
  if (erro.status === 0) return SEM_CONEXAO;
  if (erro.status === 400) return 'Valor inválido. O saldo inicial precisa ser de pelo menos R$ 1,00.';
  if (erro.status === 409) return 'Sua carteira já foi configurada. Atualize a página para ver seu painel.';
  return 'Algo deu errado ao criar sua carteira. Tente novamente em instantes.';
}

export function mensagemErroMovimentacao(erro: HttpErrorResponse): string {
  if (erro.status === 0) return SEM_CONEXAO;
  if (erro.status === 400) return 'Confira os dados: o valor mínimo é R$ 1,00 e a descrição é obrigatória.';
  if (erro.status === 404) return 'Você ainda não tem uma carteira. Configure-a na visão geral.';
  return 'Algo deu errado ao registrar a movimentação. Tente novamente em instantes.';
}
