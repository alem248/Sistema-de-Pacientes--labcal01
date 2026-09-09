package com.alex.paciente.dto;

import com.alex.paciente.enums.CategoriaAntecedente;
import com.alex.paciente.enums.TipoAntecedente;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record AntecedenteRequestDTO(
        @NotNull(message = "El tipo es obligatorio (PERSONAL/FAMILIAR)") TipoAntecedente tipo,
        @NotNull(message = "La categoría es obligatoria") CategoriaAntecedente categoria,
        @NotBlank(message = "La descripción es obligatoria") String descripcion,
        LocalDate fechaDiagnostico
) {}
