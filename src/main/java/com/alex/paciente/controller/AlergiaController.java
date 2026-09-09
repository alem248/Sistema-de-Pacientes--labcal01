package com.alex.paciente.controller;

import com.alex.paciente.dto.AlergiaRequestDTO;
import com.alex.paciente.dto.AlergiaResponseDTO;
import com.alex.paciente.enums.TipoAlergia;
import com.alex.paciente.service.AlergiaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Req.5 - Alergias: medicamentos, alimentos, otras + reacción presentada.
 */
@RestController
@RequiredArgsConstructor
public class AlergiaController {

    private final AlergiaService alergiaService;

    @PostMapping("/api/v1/alergias")
    public ResponseEntity<AlergiaResponseDTO> crear(@Valid @RequestBody AlergiaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(alergiaService.crear(dto));
    }

    @GetMapping("/api/v1/alergias")
    public ResponseEntity<List<AlergiaResponseDTO>> listar(
            @RequestParam(required = false) TipoAlergia tipo) {
        if (tipo != null) return ResponseEntity.ok(alergiaService.listarPorTipo(tipo));
        return ResponseEntity.ok(alergiaService.listarTodas());
    }

    @GetMapping("/api/v1/alergias/{id}")
    public ResponseEntity<AlergiaResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(alergiaService.obtenerPorId(id));
    }

    @PutMapping("/api/v1/alergias/{id}")
    public ResponseEntity<AlergiaResponseDTO> actualizar(@PathVariable Long id,
                                                        @Valid @RequestBody AlergiaRequestDTO dto) {
        return ResponseEntity.ok(alergiaService.actualizar(id, dto));
    }

    @DeleteMapping("/api/v1/alergias/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        alergiaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // Relación Paciente <-> Alergia
    @PostMapping("/api/v1/pacientes/{pacienteId}/alergias")
    public ResponseEntity<AlergiaResponseDTO.PacienteAlergiaResponseDTO> asignar(
            @PathVariable Long pacienteId,
            @Valid @RequestBody AlergiaRequestDTO.AsignarAlergiaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(alergiaService.asignarAPaciente(pacienteId, dto));
    }

    @GetMapping("/api/v1/pacientes/{pacienteId}/alergias")
    public ResponseEntity<List<AlergiaResponseDTO.PacienteAlergiaResponseDTO>> listarPorPaciente(
            @PathVariable Long pacienteId) {
        return ResponseEntity.ok(alergiaService.listarPorPaciente(pacienteId));
    }

    @DeleteMapping("/api/v1/pacientes/{pacienteId}/alergias/{alergiaId}")
    public ResponseEntity<Void> retirar(@PathVariable Long pacienteId,
                                       @PathVariable Long alergiaId) {
        alergiaService.retirarDePaciente(pacienteId, alergiaId);
        return ResponseEntity.noContent().build();
    }
}
