package com.alex.paciente.service;

import com.alex.paciente.audit.Auditable;
import com.alex.paciente.entity.Cita;
import com.alex.paciente.entity.Paciente;
import com.alex.paciente.repository.CitaRepository;
import com.alex.paciente.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gestion de citas (CRUD relacionado Paciente - Cita).
 * Cada operacion queda registrada en la bitacora via @Auditable.
 */
@Service
@RequiredArgsConstructor
public class CitaService {

    private final CitaRepository citaRepository;
    private final PacienteRepository pacienteRepository;

    public List<Cita> listar() {
        return citaRepository.findAllByOrderByFechaHoraAsc();
    }

    public Cita obtener(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada: " + id));
    }

    @Transactional
    @Auditable(operacion = "REGISTRO", entidad = "Cita")
    public Cita crear(Cita cita, Long pacienteId) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado: " + pacienteId));
        cita.setId(null);
        cita.setPaciente(paciente);
        if (cita.getEstado() == null) {
            cita.setEstado(Cita.EstadoCita.PENDIENTE);
        }
        return citaRepository.save(cita);
    }

    @Transactional
    @Auditable(operacion = "MODIFICACION", entidad = "Cita", idArgIndex = 0)
    public Cita cambiarEstado(Long id, Cita.EstadoCita estado) {
        Cita cita = obtener(id);
        cita.setEstado(estado);
        return citaRepository.save(cita);
    }

    @Transactional
    @Auditable(operacion = "ELIMINACION", entidad = "Cita", idArgIndex = 0)
    public void eliminar(Long id) {
        citaRepository.delete(obtener(id));
    }
}
