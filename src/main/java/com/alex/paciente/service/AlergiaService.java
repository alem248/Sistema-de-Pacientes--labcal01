package com.alex.paciente.service;

import com.alex.paciente.dto.AlergiaRequestDTO;
import com.alex.paciente.dto.AlergiaResponseDTO;
import com.alex.paciente.enums.TipoAlergia;

import java.util.List;

public interface AlergiaService {
    AlergiaResponseDTO crear(AlergiaRequestDTO dto);
    List<AlergiaResponseDTO> listarTodas();
    List<AlergiaResponseDTO> listarPorTipo(TipoAlergia tipo);
    AlergiaResponseDTO obtenerPorId(Long id);
    AlergiaResponseDTO actualizar(Long id, AlergiaRequestDTO dto);
    void eliminar(Long id);

    // N-M Paciente <-> Alergia con reacción
    AlergiaResponseDTO.PacienteAlergiaResponseDTO asignarAPaciente(Long pacienteId, AlergiaRequestDTO.AsignarAlergiaDTO dto);
    List<AlergiaResponseDTO.PacienteAlergiaResponseDTO> listarPorPaciente(Long pacienteId);
    void retirarDePaciente(Long pacienteId, Long alergiaId);
}
