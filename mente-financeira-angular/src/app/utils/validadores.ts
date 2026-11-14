import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/** Exige algo@dominio.ext (o Validators.email aceita "a@b", sem extensão). */
export const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

/** Pelo menos uma letra e um número. */
export const SENHA_PATTERN = /(?=.*[A-Za-z])(?=.*\d)/;

export const SENHA_TAMANHO_MINIMO = 8;

/** Validador de grupo: marca `senhasDiferentes` quando os dois campos não batem. */
export function senhasIguais(campoSenha: string, campoConfirmacao: string): ValidatorFn {
  return (grupo: AbstractControl): ValidationErrors | null => {
    const senha = grupo.get(campoSenha)?.value;
    const confirmacao = grupo.get(campoConfirmacao)?.value;
    if (!confirmacao) return null;
    return senha === confirmacao ? null : { senhasDiferentes: true };
  };
}
