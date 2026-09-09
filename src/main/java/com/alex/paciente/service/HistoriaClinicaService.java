package com.alex.paciente.service;

import com.alex.paciente.dto.HistoriaClinicaDTO;
import com.alex.paciente.entity.HistoriaClinica;
import com.alex.paciente.entity.Paciente;
import com.alex.paciente.exception.DuplicateResourceException;
import com.alex.paciente.exception.ResourceNotFoundException;
import com.alex.paciente.repository.HistoriaClinicaRepository;
import com.alex.paciente.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class HistoriaClinicaService {

    private final HistoriaClinicaRepository historiaRepository;
    private final PacienteRepository pacienteRepository;

    public HistoriaClinica crear(Long pacienteId, HistoriaClinicaDTO dto) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró paciente con id " + pacienteId));
        if (historiaRepository.findByPacienteId(pacienteId).isPresent())
            throw new DuplicateResourceException("El paciente ya tiene historia clínica");
        if (historiaRepository.existsByNumeroHistoria(dto.numeroHistoria()))
            throw new DuplicateResourceException("Ya existe la historia " + dto.numeroHistoria());
        HistoriaClinica h = HistoriaClinica.builder()
                .numeroHistoria(dto.numeroHistoria().trim())
                .fechaApertura(dto.fechaApertura())
                .observaciones(dto.observaciones())
                .paciente(paciente)
                .build();
        return historiaRepository.save(h);
    }

    @Transactional(readOnly = true)
    public HistoriaClinica obtenerPorPaciente(Long pacienteId) {
        return historiaRepository.findByPacienteId(pacienteId)
                .orElseThrow(() -> new ResourceNotFoundException("El paciente no tiene historia clínica"));
    }

    public HistoriaClinica actualizar(Long pacienteId, HistoriaClinicaDTO dto) {
        HistoriaClinica h = obtenerPorPaciente(pacienteId);
        if (!h.getNumeroHistoria().equals(dto.numeroHistoria())
                && historiaRepository.existsByNumeroHistoria(dto.numeroHistoria()))
            throw new DuplicateResourceException("Ya existe la historia " + dto.numeroHistoria());
        h.setNumeroHistoria(dto.numeroHistoria().trim());
        h.setFechaApertura(dto.fechaApertura());
        h.setObservaciones(dto.observaciones());
        return historiaRepository.save(h);
    }
}
