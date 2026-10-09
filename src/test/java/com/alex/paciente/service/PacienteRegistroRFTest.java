package com.alex.paciente.service;

import com.alex.paciente.entity.Paciente;
import com.alex.paciente.repository.AntecedenteRepository;
import com.alex.paciente.repository.ContactoEmergenciaRepository;
import com.alex.paciente.repository.PacienteRepository;
import com.alex.paciente.repository.SeguroPacienteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas de los requisitos principales RF-PAC-01 (registro) y RF-PAC-02
 * (validacion de numero de documento no duplicado) a nivel de servicio.
 */
@ExtendWith(MockitoExtension.class)
class PacienteRegistroRFTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private ContactoEmergenciaRepository contactoRepository;

    @Mock
    private SeguroPacienteRepository seguroRepository;

    @Mock
    private AntecedenteRepository antecedenteRepository;

    @InjectMocks
    private PacienteServiceImpl pacienteService;

    @Test
    @DisplayName("RF-PAC-01: registra paciente y genera codigo PAC-000001")
    void registrarPaciente_generaCodigo() {
        when(pacienteRepository.existsByNumeroDocumento("12345678")).thenReturn(false);
        when(pacienteRepository.findTopByOrderByIdDesc()).thenReturn(Optional.empty());
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(i -> i.getArgument(0));

        Paciente nuevo = Paciente.builder()
                .numeroDocumento("12345678")
                .nombres("Juan")
                .apellidoPaterno("Perez")
                .build();

        Paciente guardado = pacienteService.registrarPaciente(nuevo);

        assertThat(guardado.getCodigoPaciente()).isEqualTo("PAC-000001");
        verify(pacienteRepository).save(any(Paciente.class));
    }

    @Test
    @DisplayName("RF-PAC-02: rechaza numero de documento ya registrado")
    void registrarPaciente_documentoDuplicado() {
        when(pacienteRepository.existsByNumeroDocumento("12345678")).thenReturn(true);

        Paciente duplicado = Paciente.builder()
                .numeroDocumento("12345678")
                .nombres("Juan")
                .apellidoPaterno("Perez")
                .build();

        assertThatThrownBy(() -> pacienteService.registrarPaciente(duplicado))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya est");

        verify(pacienteRepository, never()).save(any(Paciente.class));
    }
}
