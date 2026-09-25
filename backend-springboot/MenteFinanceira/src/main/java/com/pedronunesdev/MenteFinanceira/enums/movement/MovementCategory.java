package com.pedronunesdev.MenteFinanceira.enums.movement;

public enum MovementCategory {

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

    MovementCategory(String enumValue) {
        this.enumValue = enumValue;
    }

    public static MovementCategory from(String valorBuscado) {

        for (MovementCategory movementCategory : values()) {
            if (movementCategory.enumValue.equalsIgnoreCase(valorBuscado)) {
                return movementCategory;
            }
        }

        throw new IllegalArgumentException("Valor: " + valorBuscado + " não encontrado para o ENUM");
    }
}