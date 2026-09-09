package com.alex.paciente.entity.enums;

public enum EstadoCobertura {
    ACTIVO("Activo"),
    VENCIDO("Vencido"),
    SUSPENDIDO("Suspendido"),
    INACTIVO("Inactivo");

    private final String displayName;

    EstadoCobertura(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
