package com.alex.paciente.entity.enums;

public enum CategoriaAntecedente {
    PERSONAL("Personal"),
    FAMILIAR("Familiar"),
    ALERGIA("Alergia");

    private final String displayName;

    CategoriaAntecedente(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
