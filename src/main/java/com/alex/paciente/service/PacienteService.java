package com.alex.paciente.service;

import com.alex.paciente.dto.PacienteRequestDTO;
import com.alex.paciente.dto.PacienteResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PacienteService {
    PacienteResponseDTO crear(PacienteRequestDTO dto);
    List<PacienteResponseDTO> listarTodos();
    Page<PacienteResponseDTO> listarPaginado(Pageable pageable);
    PacienteResponseDTO obtenerPorId(Long id);
    PacienteResponseDTO actualizar(Long id, PacienteRequestDTO dto);
    void eliminar(Long id);

    // Req.6 - Búsquedas
    PacienteResponseDTO buscarPorDni(String dni);
    PacienteResponseDTO buscarPorCodigo(String codigo);
    List<PacienteResponseDTO> buscarPorNombres(String nombres);
    List<PacienteResponseDTO> buscarPorApellidos(String apellidos);
    List<PacienteResponseDTO> buscarPorTelefono(String telefono);
    PacienteResponseDTO buscarPorHistoria(String numeroHistoria);
    Page<PacienteResponseDTO> busquedaAvanzada(String dni, String codigo, String nombres,
                                              String apellidos, String telefono,
                                              String numeroHistoria, Pageable pageable);
    Page<PacienteResponseDTO> busquedaGeneral(String q, Pageable pageable);
}
