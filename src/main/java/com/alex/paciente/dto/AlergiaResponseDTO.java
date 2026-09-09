package com.alex.paciente.dto;

import com.alex.paciente.enums.SeveridadAlergia;
import com.alex.paciente.enums.TipoAlergia;
import java.time.LocalDate;

public record AlergiaResponseDTO(Long id, String nombre, TipoAlergia tipo, String descripcion) {
    public record PacienteAlergiaResponseDTO(
            Long id,
            Long pacienteId,
            Long alergiaId,
            String alergiaNombre,
            TipoAlergia alergiaTipo,
            String reaccion,
            SeveridadAlergia severidad,
            LocalDate fechaRegistro,
            String observaciones
    ) {}
}
