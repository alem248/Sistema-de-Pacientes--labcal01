package com.alex.paciente.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record PacienteRequestDTO(
        @NotBlank(message = "El código es obligatorio") @Size(max = 20) String codigo,
        @NotBlank(message = "El DNI es obligatorio") @Size(min = 8, max = 15) String dni,
        @NotBlank(message = "Los nombres son obligatorios") @Size(max = 100) String nombres,
        @NotBlank(message = "El apellido paterno es obligatorio") @Size(max = 80) String apellidoPaterno,
        @Size(max = 80) String apellidoMaterno,
        @Size(max = 20) String telefono,
        @Email(message = "Email inválido") @Size(max = 120) String email,
        LocalDate fechaNacimiento,
        @Size(max = 20) String sexo,
        @Size(max = 200) String direccion
) {}
