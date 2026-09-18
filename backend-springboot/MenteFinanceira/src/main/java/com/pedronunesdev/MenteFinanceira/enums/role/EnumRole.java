package com.pedronunesdev.MenteFinanceira.enums.role;

public enum EnumRole {

    ROLE_USUARIO("ROLE_USUARIO"),
    ROLE_ADMIN("ROLE_ADMIN"),
    ROLE_MODERADOR("ROLE_MODERADOR"),
    ROLE_PLUS("ROLE_PLUS");

    private String enumValue;

    EnumRole(String enumValue) {
        this.enumValue = enumValue;
    }

    public EnumRole from(String valorBuscado){

        for (EnumRole enumRole : values()){
            if (enumRole.enumValue.equalsIgnoreCase(valorBuscado)){
                return enumRole;
            }
        }

        throw new IllegalArgumentException("Valor: "+valorBuscado+" não encontrado para o ENUM");
    }
}