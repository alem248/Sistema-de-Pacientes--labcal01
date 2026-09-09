package com.alex.paciente.entity.enums;

public enum EstadoCivil {
    SOLTERO("Soltero(a)"),
    CASADO("Casado(a)"),
    VIUDO("Viudo(a)"),
    DIVORCIADO("Divorciado(a)"),
    CONVIVIENTE("Conviviente"),
    SEPARADO("Separado(a)");

    private final String displayName;

    EstadoCivil(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
