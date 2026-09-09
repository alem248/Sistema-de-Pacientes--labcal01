package com.alex.paciente.entity.enums;

public enum TipoDocumento {
    DNI("DNI"),
    CE("Carné de Extranjería"),
    PASAPORTE("Pasaporte"),
    CARNET_EXTRANJERIA("Carné de Extranjería"),
    OTRO("Otro");

    private final String displayName;

    TipoDocumento(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
