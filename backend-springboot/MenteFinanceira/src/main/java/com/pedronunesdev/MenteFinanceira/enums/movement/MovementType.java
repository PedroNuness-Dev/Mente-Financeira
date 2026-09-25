package com.pedronunesdev.MenteFinanceira.enums.movement;

public enum MovementType {

    ENTRADA("ENTRADA"),
    RETIRADA("RETIRADA");

    private String enumValue;

    MovementType(String enumValue) {
        this.enumValue = enumValue;
    }

    public static MovementType from(String valorBuscado){

        for (MovementType movementType : values()){
            if (movementType.enumValue.equalsIgnoreCase(valorBuscado)){
                return movementType;
            }
        }

        throw new IllegalArgumentException("Valor: "+valorBuscado+" não encontrado para o ENUM");
    }
}
