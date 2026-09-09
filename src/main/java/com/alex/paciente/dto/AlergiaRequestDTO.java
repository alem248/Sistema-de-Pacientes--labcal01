package com.alex.paciente.dto;

import com.alex.paciente.enums.TipoAlergia;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import com.alex.paciente.enums.SeveridadAlergia;

public record AlergiaRequestDTO(
        @NotBlank(message = "El nombre de la alergia es obligatorio") @Size(max = 120) String nombre,
        @NotNull(message = "El tipo es obligatorio (MEDICAMENTO/ALIMENTO/OTRA)") TipoAlergia tipo,
        String descripcion
) {
    public record AsignarAlergiaDTO(
            @NotNull(message = "alergiaId es obligatorio") Long alergiaId,
            @NotBlank(message = "La reacción presentada es obligatoria") String reaccion,
            SeveridadAlergia severidad,
            LocalDate fechaRegistro,
            String observaciones
    ) {}
}
