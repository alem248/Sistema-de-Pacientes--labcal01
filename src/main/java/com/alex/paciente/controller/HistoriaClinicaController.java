package com.alex.paciente.controller;

import com.alex.paciente.dto.HistoriaClinicaDTO;
import com.alex.paciente.entity.HistoriaClinica;
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
    public ResponseEntity<HistoriaClinica> crear(@PathVariable Long pacienteId,
                                                @Valid @RequestBody HistoriaClinicaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(historiaService.crear(pacienteId, dto));
    }

    @GetMapping
    public ResponseEntity<HistoriaClinica> obtener(@PathVariable Long pacienteId) {
        return ResponseEntity.ok(historiaService.obtenerPorPaciente(pacienteId));
    }

    @PutMapping
    public ResponseEntity<HistoriaClinica> actualizar(@PathVariable Long pacienteId,
                                                     @Valid @RequestBody HistoriaClinicaDTO dto) {
        return ResponseEntity.ok(historiaService.actualizar(pacienteId, dto));
    }
}
