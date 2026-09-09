package com.alex.paciente.service;

import com.alex.paciente.dto.AntecedenteRequestDTO;
import com.alex.paciente.dto.AntecedenteResponseDTO;
import com.alex.paciente.entity.Antecedente;
import com.alex.paciente.entity.Paciente;
import com.alex.paciente.enums.CategoriaAntecedente;
import com.alex.paciente.enums.TipoAntecedente;
import com.alex.paciente.exception.ResourceNotFoundException;
import com.alex.paciente.repository.AntecedenteRepository;
import com.alex.paciente.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Req.5 - Lógica de antecedentes personales/familiares.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AntecedenteServiceImpl implements AntecedenteService {

    private final AntecedenteRepository antecedenteRepository;
    private final PacienteRepository pacienteRepository;

    @Override
    public AntecedenteResponseDTO crear(Long pacienteId, AntecedenteRequestDTO dto) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró paciente con id " + pacienteId));
        Antecedente a = Antecedente.builder()
                .tipo(dto.tipo())
                .categoria(dto.categoria())
                .descripcion(dto.descripcion().trim())
                .fechaDiagnostico(dto.fechaDiagnostico())
                .activo(true)
                .paciente(paciente)
                .build();
        return toDTO(antecedenteRepository.save(a));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AntecedenteResponseDTO> listarPorPaciente(Long pacienteId) {
        verificarPaciente(pacienteId);
        return antecedenteRepository.findByPacienteId(pacienteId).stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AntecedenteResponseDTO> listarPorPacienteYTipo(Long pacienteId, TipoAntecedente tipo) {
        verificarPaciente(pacienteId);
        return antecedenteRepository.findByPacienteIdAndTipo(pacienteId, tipo).stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AntecedenteResponseDTO> listarPorPacienteYCategoria(Long pacienteId, CategoriaAntecedente categoria) {
        verificarPaciente(pacienteId);
        return antecedenteRepository.findByPacienteIdAndCategoria(pacienteId, categoria).stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AntecedenteResponseDTO obtenerPorId(Long id) {
        return toDTO(getEntity(id));
    }

    @Override
    public AntecedenteResponseDTO actualizar(Long id, AntecedenteRequestDTO dto) {
        Antecedente a = getEntity(id);
        a.setTipo(dto.tipo());
        a.setCategoria(dto.categoria());
        a.setDescripcion(dto.descripcion().trim());
        a.setFechaDiagnostico(dto.fechaDiagnostico());
        return toDTO(antecedenteRepository.save(a));
    }

    @Override
    public void eliminar(Long id) {
        antecedenteRepository.delete(getEntity(id));
    }

    private Antecedente getEntity(Long id) {
        return antecedenteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró antecedente con id " + id));
    }

    private void verificarPaciente(Long pacienteId) {
        if (!pacienteRepository.existsById(pacienteId))
            throw new ResourceNotFoundException("No se encontró paciente con id " + pacienteId);
    }

    private AntecedenteResponseDTO toDTO(Antecedente a) {
        Paciente p = a.getPaciente();
        String nombre = p.getNombres() + " " + p.getApellidoPaterno();
        return new AntecedenteResponseDTO(
                a.getId(), a.getTipo(), a.getCategoria(), a.getDescripcion(),
                a.getFechaDiagnostico(), a.getFechaRegistro(), a.getActivo(),
                p.getId(), nombre);
    }
}
