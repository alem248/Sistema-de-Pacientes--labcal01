package com.alex.paciente.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record HistoriaClinicaDTO(
        @NotBlank(message = "El número de historia es obligatorio") @Size(max = 30) String numeroHistoria,
        LocalDate fechaApertura,
        String observaciones
) {}
