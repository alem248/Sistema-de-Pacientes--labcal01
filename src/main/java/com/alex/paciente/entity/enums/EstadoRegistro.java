package com.alex.paciente.entity.enums;

public enum EstadoRegistro {
    ACTIVO("Activo"),
    INACTIVO("Inactivo"),
    FALLECIDO("Fallecido");

    private final String displayName;

    EstadoRegistro(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
