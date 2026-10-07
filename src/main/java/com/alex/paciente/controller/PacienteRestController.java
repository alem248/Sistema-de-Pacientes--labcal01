package com.alex.paciente.controller;

import com.alex.paciente.dto.PacienteCreateDTO;
import com.alex.paciente.dto.PacienteResponseDTO;
import com.alex.paciente.service.PacienteConsultaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Req.6 - Búsqueda de pacientes: DNI, código, nombres, apellidos, teléfono, historia clínica.
 * CRUD base + endpoints de consulta.
 */
@RestController
@RequestMapping("/api/v1/pacientes")
@RequiredArgsConstructor
public class PacienteRestController {

    private final PacienteConsultaService pacienteService;

    @PostMapping
    public ResponseEntity<PacienteResponseDTO> crear(@Valid @RequestBody PacienteCreateDTO dto) {
        Paciente paciente = pacienteService.registrarPacienteDesdeDTO(dto);
        // Convertir a DTO de respuesta
        PacienteResponseDTO dtoRespuesta = new PacienteResponseDTO(
                paciente.getId(),
                paciente.getCodigoPaciente(),
                paciente.getNumeroDocumento(),
                paciente.getNombres(),
                paciente.getApellidoPaterno(),
                paciente.getApellidoMaterno(),
                paciente.getTelefono(),
                paciente.getCorreo(),
                paciente.getFechaNacimiento(),
                paciente.getSexo() != null ? paciente.getSexo().name() : null,
                paciente.getDireccion(),
                paciente.getEstadoRegistro() != null ? paciente.getEstadoRegistro().name().equals("ACTIVO") : null,
                paciente.getFechaRegistro(),
                null, // numeroHistoria - se obtendría de historia clínica
                0,    // totalAntecedentes
                0     // totalAlergias
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoRespuesta);
    }

    // ---------- Req.6: búsquedas específicas ----------
    @GetMapping("/dni/{dni}")
    public ResponseEntity<PacienteResponseDTO> porDni(@PathVariable String dni) {
        return ResponseEntity.ok(pacienteService.buscarPorDni(dni));
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<PacienteResponseDTO> porCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(pacienteService.buscarPorCodigo(codigo));
    }

    @GetMapping("/historia/{numero}")
    public ResponseEntity<PacienteResponseDTO> porHistoria(@PathVariable String numero) {
        return ResponseEntity.ok(pacienteService.buscarPorHistoria(numero));
    }

    @GetMapping("/buscar-nombres")
    public ResponseEntity<List<PacienteResponseDTO>> porNombres(@RequestParam String nombres) {
        return ResponseEntity.ok(pacienteService.buscarPorNombres(nombres));
    }

    @GetMapping("/buscar-apellidos")
    public ResponseEntity<List<PacienteResponseDTO>> porApellidos(@RequestParam String apellidos) {
        return ResponseEntity.ok(pacienteService.buscarPorApellidos(apellidos));
    }

    @GetMapping("/buscar-telefono")
    public ResponseEntity<List<PacienteResponseDTO>> porTelefono(@RequestParam String telefono) {
        return ResponseEntity.ok(pacienteService.buscarPorTelefono(telefono));
    }

    /** * Búsqueda avanzada combinada. Todos los filtros son opcionales.
     * Ej: GET /api/v1/pacientes/search?dni=123&nombres=juan&apellidos=perez&telefono=999&historia=HC-001&page=0&size=10
     */
    @GetMapping("/search")
    public ResponseEntity<Page<PacienteResponseDTO>> busquedaAvanzada(
            @RequestParam(required = false) String dni,
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String nombres,
            @RequestParam(required = false) String apellidos,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false, name = "historia") String numeroHistoria,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(pacienteService.busquedaAvanzada(
                dni, codigo, nombres, apellidos, telefono, numeroHistoria, pageable));
    }

    /** Búsqueda general con un solo texto.
     * Ej: GET /api/v1/pacientes/buscar?q=perez
     */
    @GetMapping("/buscar")
    public ResponseEntity<Page<PacienteResponseDTO>> busquedaGeneral(
            @RequestParam String q,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(pacienteService.busquedaGeneral(q, pageable));
    }

    // ---------- Nueva funcionalidad: Verificar disponibilidad de código ----------
    @GetMapping("/api/verificar-codigo/{codigo}")
    @ResponseBody
    public String verificarCodigoDisponible(@PathVariable String codigo) {
        boolean existe = pacienteService.existeCodigoUnico(codigo);
        return existe ? "EXISTE" : "DISPONIBLE";
    }