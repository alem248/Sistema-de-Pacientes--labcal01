package com.alex.paciente.dto;

import java.time.LocalDate;

public record HistoriaClinicaResponseDTO(
        Long id,
        String numeroHistoria,
        LocalDate fechaApertura,
        String observaciones,
        Long pacienteId
) {}
