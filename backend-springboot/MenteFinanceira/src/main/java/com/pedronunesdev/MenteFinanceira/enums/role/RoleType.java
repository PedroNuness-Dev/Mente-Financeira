package com.pedronunesdev.MenteFinanceira.enums.role;

public enum RoleType {

    ROLE_USER("ROLE_USER"),
    ROLE_ADMIN("ROLE_ADMIN"),
    ROLE_MODERATOR("ROLE_MODERATOR"),
    ROLE_PLUS("ROLE_PLUS");

    private String enumValue;

    RoleType(String enumValue) {
        this.enumValue = enumValue;
    }

    public static RoleType from(String valueSought){

        for (RoleType roleType : values()){
            if (roleType.enumValue.equalsIgnoreCase(valueSought)){
                return roleType;
            }
        }

        throw new IllegalArgumentException("Value: "+valueSought+" not found for the ENUM");
    }
}