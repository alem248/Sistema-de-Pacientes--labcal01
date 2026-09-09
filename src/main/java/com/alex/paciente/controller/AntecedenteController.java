package com.alex.paciente.controller;

import com.alex.paciente.dto.AntecedenteRequestDTO;
import com.alex.paciente.dto.AntecedenteResponseDTO;
import com.alex.paciente.enums.CategoriaAntecedente;
import com.alex.paciente.enums.TipoAntecedente;
import com.alex.paciente.service.AntecedenteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Req.5 - Antecedentes personales y familiares.
 */
@RestController
@RequiredArgsConstructor
public class AntecedenteController {

    private final AntecedenteService antecedenteService;

    @PostMapping("/api/v1/pacientes/{pacienteId}/antecedentes")
    public ResponseEntity<AntecedenteResponseDTO> crear(@PathVariable Long pacienteId,
                                                       @Valid @RequestBody AntecedenteRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(antecedenteService.crear(pacienteId, dto));
    }

    @GetMapping("/api/v1/pacientes/{pacienteId}/antecedentes")
    public ResponseEntity<List<AntecedenteResponseDTO>> listarPorPaciente(
            @PathVariable Long pacienteId,
            @RequestParam(required = false) TipoAntecedente tipo,
            @RequestParam(required = false) CategoriaAntecedente categoria) {
        if (tipo != null) return ResponseEntity.ok(antecedenteService.listarPorPacienteYTipo(pacienteId, tipo));
        if (categoria != null) return ResponseEntity.ok(antecedenteService.listarPorPacienteYCategoria(pacienteId, categoria));
        return ResponseEntity.ok(antecedenteService.listarPorPaciente(pacienteId));
    }

    @GetMapping("/api/v1/antecedentes/{id}")
    public ResponseEntity<AntecedenteResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(antecedenteService.obtenerPorId(id));
    }

    @PutMapping("/api/v1/antecedentes/{id}")
    public ResponseEntity<AntecedenteResponseDTO> actualizar(@PathVariable Long id,
                                                            @Valid @RequestBody AntecedenteRequestDTO dto) {
        return ResponseEntity.ok(antecedenteService.actualizar(id, dto));
    }

    @DeleteMapping("/api/v1/antecedentes/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        antecedenteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
