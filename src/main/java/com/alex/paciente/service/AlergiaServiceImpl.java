package com.alex.paciente.service;

import com.alex.paciente.dto.AlergiaRequestDTO;
import com.alex.paciente.dto.AlergiaResponseDTO;
import com.alex.paciente.entity.Alergia;
import com.alex.paciente.entity.Paciente;
import com.alex.paciente.entity.PacienteAlergia;
import com.alex.paciente.enums.TipoAlergia;
import com.alex.paciente.exception.DuplicateResourceException;
import com.alex.paciente.exception.ResourceNotFoundException;
import com.alex.paciente.repository.AlergiaRepository;
import com.alex.paciente.repository.PacienteAlergiaRepository;
import com.alex.paciente.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Req.5 - Alergias (medicamentos, alimentos, otras + reacción presentada).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AlergiaServiceImpl implements AlergiaService {

    private final AlergiaRepository alergiaRepository;
    private final PacienteRepository pacienteRepository;
    private final PacienteAlergiaRepository pacienteAlergiaRepository;

    @Override
    public AlergiaResponseDTO crear(AlergiaRequestDTO dto) {
        if (alergiaRepository.existsByNombreIgnoreCase(dto.nombre().trim()))
            throw new DuplicateResourceException("Ya existe la alergia " + dto.nombre());
        Alergia a = Alergia.builder()
                .nombre(dto.nombre().trim())
                .tipo(dto.tipo())
                .descripcion(dto.descripcion())
                .build();
        Alergia saved = alergiaRepository.save(a);
        return new AlergiaResponseDTO(saved.getId(), saved.getNombre(), saved.getTipo(), saved.getDescripcion());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlergiaResponseDTO> listarTodas() {
        return alergiaRepository.findAll().stream()
                .map(a -> new AlergiaResponseDTO(a.getId(), a.getNombre(), a.getTipo(), a.getDescripcion()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlergiaResponseDTO> listarPorTipo(TipoAlergia tipo) {
        return alergiaRepository.findByTipo(tipo).stream()
                .map(a -> new AlergiaResponseDTO(a.getId(), a.getNombre(), a.getTipo(), a.getDescripcion()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AlergiaResponseDTO obtenerPorId(Long id) {
        Alergia a = getAlergia(id);
        return new AlergiaResponseDTO(a.getId(), a.getNombre(), a.getTipo(), a.getDescripcion());
    }

    @Override
    public AlergiaResponseDTO actualizar(Long id, AlergiaRequestDTO dto) {
        Alergia a = getAlergia(id);
        alergiaRepository.findByNombreIgnoreCase(dto.nombre().trim())
                .filter(x -> !x.getId().equals(id))
                .ifPresent(x -> { throw new DuplicateResourceException("Ya existe la alergia " + dto.nombre()); });
        a.setNombre(dto.nombre().trim());
        a.setTipo(dto.tipo());
        a.setDescripcion(dto.descripcion());
        Alergia saved = alergiaRepository.save(a);
        return new AlergiaResponseDTO(saved.getId(), saved.getNombre(), saved.getTipo(), saved.getDescripcion());
    }

    @Override
    public void eliminar(Long id) {
        alergiaRepository.delete(getAlergia(id));
    }

    @Override
    public AlergiaResponseDTO.PacienteAlergiaResponseDTO asignarAPaciente(Long pacienteId, AlergiaRequestDTO.AsignarAlergiaDTO dto) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró paciente con id " + pacienteId));
        Alergia alergia = getAlergia(dto.alergiaId());
        if (pacienteAlergiaRepository.existsByPacienteIdAndAlergiaId(pacienteId, dto.alergiaId()))
            throw new DuplicateResourceException("El paciente ya tiene registrada la alergia " + alergia.getNombre());
        PacienteAlergia pa = PacienteAlergia.builder()
                .paciente(paciente)
                .alergia(alergia)
                .reaccion(dto.reaccion().trim())
                .severidad(dto.severidad())
                .fechaRegistro(dto.fechaRegistro())
                .observaciones(dto.observaciones())
                .build();
        return toDTO(pacienteAlergiaRepository.save(pa));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlergiaResponseDTO.PacienteAlergiaResponseDTO> listarPorPaciente(Long pacienteId) {
        if (!pacienteRepository.existsById(pacienteId))
            throw new ResourceNotFoundException("No se encontró paciente con id " + pacienteId);
        return pacienteAlergiaRepository.findByPacienteId(pacienteId).stream().map(this::toDTO).toList();
    }

    @Override
    public void retirarDePaciente(Long pacienteId, Long alergiaId) {
        PacienteAlergia pa = pacienteAlergiaRepository.findByPacienteIdAndAlergiaId(pacienteId, alergiaId)
                .orElseThrow(() -> new ResourceNotFoundException("El paciente no tiene registrada esa alergia"));
        pacienteAlergiaRepository.delete(pa);
    }

    private Alergia getAlergia(Long id) {
        return alergiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró alergia con id " + id));
    }

    private AlergiaResponseDTO.PacienteAlergiaResponseDTO toDTO(PacienteAlergia pa) {
        return new AlergiaResponseDTO.PacienteAlergiaResponseDTO(
                pa.getId(),
                pa.getPaciente().getId(),
                pa.getAlergia().getId(),
                pa.getAlergia().getNombre(),
                pa.getAlergia().getTipo(),
                pa.getReaccion(),
                pa.getSeveridad(),
                pa.getFechaRegistro(),
                pa.getObservaciones());
    }
}
