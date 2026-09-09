package com.alex.paciente.dto;

import com.alex.paciente.enums.CategoriaAntecedente;
import com.alex.paciente.enums.TipoAntecedente;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AntecedenteResponseDTO(
        Long id,
        TipoAntecedente tipo,
        CategoriaAntecedente categoria,
        String descripcion,
        LocalDate fechaDiagnostico,
        LocalDateTime fechaRegistro,
        Boolean activo,
        Long pacienteId,
        String pacienteNombre
) {}
