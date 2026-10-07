package com.alex.paciente.controller;

import com.alex.paciente.dto.PacienteDetalleResponseDTO;
import com.alex.paciente.dto.PacienteRequestDTO;
import com.alex.paciente.dto.PacienteResponseDTO;
import com.alex.paciente.service.PacienteConsultaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RF-PAC-05: búsqueda de pacientes por documento, código, nombres y apellidos.
 * RF-PAC-06: ficha completa del paciente.
 * Seguridad en dos capas: reglas de URL (SecurityConfig) + @PreAuthorize por método.
 */
@Validated
@RestController
@RequestMapping("/api/v1/pacientes")
@RequiredArgsConstructor
public class PacienteRestController {

    private final PacienteConsultaService pacienteService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','RECEPCIONISTA')")
    public ResponseEntity<PacienteResponseDTO> crear(@Valid @RequestBody PacienteRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pacienteService.crear(dto));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','MEDICO','RECEPCIONISTA')")
    public ResponseEntity<List<PacienteResponseDTO>> listar() {
        return ResponseEntity.ok(pacienteService.listarTodos());
    }

    @GetMapping("/paginado")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','MEDICO','RECEPCIONISTA')")
    public ResponseEntity<Page<PacienteResponseDTO>> listarPaginado(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(pacienteService.listarPaginado(pageable));
    }

    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','MEDICO','RECEPCIONISTA')")
    public ResponseEntity<PacienteResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(pacienteService.obtenerPorId(id));
    }

    /** RF-PAC-06: información completa del paciente (datos + relaciones + historia clínica). */
    @GetMapping("/{id:\\d+}/completo")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','MEDICO','RECEPCIONISTA')")
    public ResponseEntity<PacienteDetalleResponseDTO> detalleCompleto(@PathVariable Long id) {
        return ResponseEntity.ok(pacienteService.obtenerDetalleCompleto(id));
    }

    @PutMapping("/{id:\\d+}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','RECEPCIONISTA')")
    public ResponseEntity<PacienteResponseDTO> actualizar(@PathVariable Long id,
                                                          @Valid @RequestBody PacienteRequestDTO dto) {
        return ResponseEntity.ok(pacienteService.actualizar(id, dto));
    }

    @DeleteMapping("/{id:\\d+}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pacienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- RF-PAC-05: búsquedas específicas con validación de parámetros ----------
    @GetMapping("/dni/{dni}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','MEDICO','RECEPCIONISTA')")
    public ResponseEntity<PacienteResponseDTO> porDni(
            @PathVariable @Pattern(regexp = "^[A-Za-z0-9]{8,20}$",
                    message = "El documento debe tener entre 8 y 20 caracteres alfanuméricos") String dni) {
        return ResponseEntity.ok(pacienteService.buscarPorDni(dni));
    }

    @GetMapping("/codigo/{codigo}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','MEDICO','RECEPCIONISTA')")
    public ResponseEntity<PacienteResponseDTO> porCodigo(
            @PathVariable @Pattern(regexp = "^[A-Za-z0-9\\-]{3,20}$",
                    message = "El código debe tener entre 3 y 20 caracteres") String codigo) {
        return ResponseEntity.ok(pacienteService.buscarPorCodigo(codigo));
    }

    @GetMapping("/historia/{numero}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','MEDICO')")
    public ResponseEntity<PacienteResponseDTO> porHistoria(
            @PathVariable @NotBlank @Size(max = 30) String numero) {
        return ResponseEntity.ok(pacienteService.buscarPorHistoria(numero));
    }

    @GetMapping("/buscar-nombres")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','MEDICO','RECEPCIONISTA')")
    public ResponseEntity<List<PacienteResponseDTO>> porNombres(
            @RequestParam @NotBlank(message = "El criterio nombres es obligatorio")
            @Size(min = 2, max = 100, message = "Ingrese al menos 2 caracteres") String nombres) {
        return ResponseEntity.ok(pacienteService.buscarPorNombres(nombres.trim()));
    }

    @GetMapping("/buscar-apellidos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','MEDICO','RECEPCIONISTA')")
    public ResponseEntity<List<PacienteResponseDTO>> porApellidos(
            @RequestParam @NotBlank(message = "El criterio apellidos es obligatorio")
            @Size(min = 2, max = 100, message = "Ingrese al menos 2 caracteres") String apellidos) {
        return ResponseEntity.ok(pacienteService.buscarPorApellidos(apellidos.trim()));
    }

    @GetMapping("/buscar-telefono")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','MEDICO','RECEPCIONISTA')")
    public ResponseEntity<List<PacienteResponseDTO>> porTelefono(
            @RequestParam @Pattern(regexp = "^[0-9+\\-\\s]{5,20}$",
                    message = "Teléfono de búsqueda inválido") String telefono) {
        return ResponseEntity.ok(pacienteService.buscarPorTelefono(telefono.trim()));
    }

    /**
     * Búsqueda avanzada combinada. Todos los filtros son opcionales.
     * Ej: GET /api/v1/pacientes/search?dni=123&nombres=juan&apellidos=perez&telefono=999&historia=HC-001&page=0&size=10
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','MEDICO','RECEPCIONISTA')")
    public ResponseEntity<Page<PacienteResponseDTO>> busquedaAvanzada(
            @RequestParam(required = false) @Size(max = 20) String dni,
            @RequestParam(required = false) @Size(max = 20) String codigo,
            @RequestParam(required = false) @Size(max = 100) String nombres,
            @RequestParam(required = false) @Size(max = 100) String apellidos,
            @RequestParam(required = false) @Size(max = 20) String telefono,
            @RequestParam(required = false, name = "historia") @Size(max = 30) String numeroHistoria,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(pacienteService.busquedaAvanzada(
                dni, codigo, nombres, apellidos, telefono, numeroHistoria, pageable));
    }

    /**
     * RF-PAC-05: búsqueda general con un solo texto (documento, código, nombres o apellidos).
     * Ej: GET /api/v1/pacientes/buscar?q=perez
     */
    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','MEDICO','RECEPCIONISTA')")
    public ResponseEntity<Page<PacienteResponseDTO>> busquedaGeneral(
            @RequestParam @NotBlank(message = "El criterio de búsqueda es obligatorio")
            @Size(min = 2, max = 100, message = "El criterio debe tener entre 2 y 100 caracteres") String q,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(pacienteService.busquedaGeneral(q.trim(), pageable));
    }
}
