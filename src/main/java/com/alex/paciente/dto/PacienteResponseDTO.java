package com.alex.paciente.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PacienteResponseDTO(
        Long id,
        String codigo,
        String dni,
        String nombres,
        String apellidoPaterno,
        String apellidoMaterno,
        String telefono,
        String email,
        LocalDate fechaNacimiento,
        String sexo,
        String direccion,
        Boolean activo,
        LocalDateTime fechaRegistro,
        String numeroHistoria,
        int totalAntecedentes,
        int totalAlergias
) {}
