package com.alex.paciente.entity.enums;

public enum Sexo {
    MASCULINO("Masculino"),
    FEMENINO("Femenino"),
    OTRO("Otro");

    private final String displayName;

    Sexo(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
