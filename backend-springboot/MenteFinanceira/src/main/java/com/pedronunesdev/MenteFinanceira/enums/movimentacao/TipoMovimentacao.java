package com.pedronunesdev.MenteFinanceira.enums.movimentacao;

public enum TipoMovimentacao {

    ENTRADA("ENRTRADA"),
    RETIRADA("RETIRADA");

    private String enumValue;

    TipoMovimentacao(String enumValue) {
        this.enumValue = enumValue;
    }

    public static TipoMovimentacao from(String valorBuscado){

        for (TipoMovimentacao tipoMovimentacao : values()){
            if (tipoMovimentacao.enumValue.equalsIgnoreCase(valorBuscado)){
                return tipoMovimentacao;
            }
        }

        throw new IllegalArgumentException("Valor: "+valorBuscado+" não encontrado para o ENUM");
    }
}
