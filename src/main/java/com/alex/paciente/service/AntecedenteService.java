package com.alex.paciente.service;

import com.alex.paciente.dto.AntecedenteRequestDTO;
import com.alex.paciente.dto.AntecedenteResponseDTO;
import com.alex.paciente.enums.CategoriaAntecedente;
import com.alex.paciente.enums.TipoAntecedente;

import java.util.List;

public interface AntecedenteService {
    AntecedenteResponseDTO crear(Long pacienteId, AntecedenteRequestDTO dto);
    List<AntecedenteResponseDTO> listarPorPaciente(Long pacienteId);
    List<AntecedenteResponseDTO> listarPorPacienteYTipo(Long pacienteId, TipoAntecedente tipo);
    List<AntecedenteResponseDTO> listarPorPacienteYCategoria(Long pacienteId, CategoriaAntecedente categoria);
    AntecedenteResponseDTO obtenerPorId(Long id);
    AntecedenteResponseDTO actualizar(Long id, AntecedenteRequestDTO dto);
    void eliminar(Long id);
}
