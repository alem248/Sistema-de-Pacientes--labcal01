package com.alex.paciente.controller;

import com.alex.paciente.dto.HistoriaClinicaDTO;
import com.alex.paciente.dto.HistoriaClinicaResponseDTO;
import com.alex.paciente.service.HistoriaClinicaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pacientes/{pacienteId}/historia")
@RequiredArgsConstructor
public class HistoriaClinicaController {

    private final HistoriaClinicaService historiaService;

    @PostMapping
    public ResponseEntity<HistoriaClinicaResponseDTO> crear(@PathVariable Long pacienteId,
                                                           @Valid @RequestBody HistoriaClinicaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(historiaService.crear(pacienteId, dto));
    }

    @GetMapping
    public ResponseEntity<HistoriaClinicaResponseDTO> obtener(@PathVariable Long pacienteId) {
        return ResponseEntity.ok(historiaService.obtenerPorPaciente(pacienteId));
    }

    @PutMapping
    public ResponseEntity<HistoriaClinicaResponseDTO> actualizar(@PathVariable Long pacienteId,
                                                                @Valid @RequestBody HistoriaClinicaDTO dto) {
        return ResponseEntity.ok(historiaService.actualizar(pacienteId, dto));
    }
}
