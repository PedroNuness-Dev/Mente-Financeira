package com.pedronunesdev.MenteFinanceira.domain.movimentacao;

public enum CategoriaMovimentacao {

    ALIMENTACAO("ALIMENTACAO"),
    MERCADO("MERCADO"),
    TRANSPORTE("TRANSPORTE"),
    MORADIA("MORADIA"),
    CONTAS_FIXAS("CONTAS_FIXAS"),
    SAUDE("SAUDE"),
    EDUCACAO("EDUCACAO"),
    LAZER("LAZER"),
    VESTUARIO("VESTUARIO"),
    ASSINATURAS("ASSINATURAS"),
    VIAGEM("VIAGEM"),
    PET("PET"),
    PRESENTES("PRESENTES"),
    INVESTIMENTOS("INVESTIMENTOS"),
    SALARIO("SALARIO"),
    RENDIMENTOS("RENDIMENTOS"),
    TRANSFERENCIA("TRANSFERENCIA"),
    DEPOSITO("DEPOSITO"),
    OUTROS("OUTROS");

    private String enumValue;

    CategoriaMovimentacao(String enumValue) {
        this.enumValue = enumValue;
    }

    public static CategoriaMovimentacao from(String valorBuscado) {

        for (CategoriaMovimentacao categoriaMovimentacao : values()) {
            if (categoriaMovimentacao.enumValue.equalsIgnoreCase(valorBuscado)) {
                return categoriaMovimentacao;
            }
        }

        throw new IllegalArgumentException("Valor: " + valorBuscado + " não encontrado para o ENUM");
    }
}