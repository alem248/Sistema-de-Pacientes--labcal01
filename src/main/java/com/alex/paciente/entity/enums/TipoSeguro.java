package com.alex.paciente.entity.enums;

public enum TipoSeguro {
    SIS("SIS"),
    ESSALUD("EsSalud"),
    PRIVADO("Privado"),
    EPS("EPS"),
    OTRO("Otro");

    private final String displayName;

    TipoSeguro(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
