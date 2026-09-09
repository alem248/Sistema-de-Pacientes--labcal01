package com.alex.Paciente.service;

import com.alex.Paciente.entity.Paciente;
import com.alex.Paciente.exception.DocumentoDuplicadoException;
import com.alex.Paciente.repository.PacienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * RF-PAC-02: Validacion del numero de documento
 */
@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;

    public PacienteService(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    /**
     * Verifica si el numero de documento ya existe
     */
    public boolean existeDocumento(String numeroDocumento) {
        return pacienteRepository.existsByNumeroDocumento(numeroDocumento);
    }

    @Transactional
    public Paciente registrarPaciente(Paciente paciente) {
        if (existeDocumento(paciente.getNumeroDocumento())) {
            throw new DocumentoDuplicadoException(
                    "El numero de documento '" + paciente.getNumeroDocumento() + "' ya esta registrado");
        }
        return pacienteRepository.save(paciente);
    }
}
