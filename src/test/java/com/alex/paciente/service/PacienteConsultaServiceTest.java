package com.alex.paciente.service;

import com.alex.paciente.dto.PacienteDetalleResponseDTO;
import com.alex.paciente.dto.PacienteResponseDTO;
import com.alex.paciente.entity.HistoriaClinica;
import com.alex.paciente.entity.Paciente;
import com.alex.paciente.entity.enums.EstadoRegistro;
import com.alex.paciente.entity.enums.Sexo;
import com.alex.paciente.exception.ResourceNotFoundException;
import com.alex.paciente.repository.HistoriaClinicaRepository;
import com.alex.paciente.repository.PacienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias del servicio de consulta con Mockito (sin BD).
 * Cubren RF-PAC-05 (búsqueda) y RF-PAC-06 (ficha completa) + gestión de errores.
 */
@ExtendWith(MockitoExtension.class)
class PacienteConsultaServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;
    @Mock
    private HistoriaClinicaRepository historiaRepository;
    @Mock
    private PacienteService pacienteService;

    @InjectMocks
    private PacienteConsultaServiceImpl service;

    private Paciente paciente;

    @BeforeEach
    void setUp() {
        paciente = Paciente.builder()
                .id(1L)
                .codigoPaciente("PAC-000001")
                .numeroDocumento("12345678")
                .nombres("Juan")
                .apellidoPaterno("Pérez")
                .apellidoMaterno("García")
                .fechaNacimiento(LocalDate.of(1990, 5, 15))
                .sexo(Sexo.MASCULINO)
                .telefono("987654321")
                .direccion("Av. Los Pinos 123")
                .distrito("Lima").provincia("Lima").departamento("Lima")
                .estadoRegistro(EstadoRegistro.ACTIVO)
                .fechaRegistro(LocalDateTime.now())
                .build();
    }

    // ---------- RF-PAC-05 ----------
    @Test
    @DisplayName("RF-PAC-05: buscarPorDni devuelve el paciente cuando existe")
    void buscarPorDniExistente() {
        when(pacienteRepository.findByDni("12345678")).thenReturn(Optional.of(paciente));

        PacienteResponseDTO dto = service.buscarPorDni("12345678");

        assertThat(dto.dni()).isEqualTo("12345678");
        assertThat(dto.codigo()).isEqualTo("PAC-000001");
        verify(pacienteRepository).findByDni("12345678");
    }

    @Test
    @DisplayName("RF-PAC-05: buscarPorDni lanza ResourceNotFoundException cuando no existe")
    void buscarPorDniNoExistente() {
        when(pacienteRepository.findByDni("00000000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorDni("00000000"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("00000000");
    }

    @Test
    @DisplayName("RF-PAC-05: buscarPorCodigo devuelve el paciente cuando existe")
    void buscarPorCodigoExistente() {
        when(pacienteRepository.findByCodigo("PAC-000001")).thenReturn(Optional.of(paciente));

        PacienteResponseDTO dto = service.buscarPorCodigo("PAC-000001");

        assertThat(dto.nombres()).isEqualTo("Juan");
    }

    @Test
    @DisplayName("RF-PAC-05: buscarPorNombres mapea la lista de resultados")
    void buscarPorNombres() {
        when(pacienteRepository.findByNombresContainingIgnoreCase("juan"))
                .thenReturn(List.of(paciente));

        List<PacienteResponseDTO> resultados = service.buscarPorNombres("juan");

        assertThat(resultados).hasSize(1);
        assertThat(resultados.get(0).apellidoPaterno()).isEqualTo("Pérez");
    }

    @Test
    @DisplayName("RF-PAC-05: búsqueda general con texto en blanco lista todo paginado")
    void busquedaGeneralVacia() {
        Page<Paciente> pagina = new PageImpl<>(List.of(paciente));
        when(pacienteRepository.findAll(any(PageRequest.class))).thenReturn(pagina);

        Page<PacienteResponseDTO> resultado = service.busquedaGeneral("  ", PageRequest.of(0, 10));

        assertThat(resultado.getTotalElements()).isEqualTo(1);
        verify(pacienteRepository, never()).busquedaGeneral(anyString(), any());
    }

    // ---------- RF-PAC-06 ----------
    @Test
    @DisplayName("RF-PAC-06: obtenerDetalleCompleto incluye historia clínica y colecciones")
    void detalleCompleto() {
        HistoriaClinica historia = HistoriaClinica.builder()
                .id(1L).numeroHistoria("HC-000001").fechaApertura(LocalDate.now())
                .observaciones("Paciente sin novedad").paciente(paciente).build();
        paciente.setHistoriaClinica(historia);
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));

        PacienteDetalleResponseDTO dto = service.obtenerDetalleCompleto(1L);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.numeroDocumento()).isEqualTo("12345678");
        assertThat(dto.nombreCompleto()).contains("Juan", "Pérez");
        assertThat(dto.edad()).isGreaterThan(0);
        assertThat(dto.numeroHistoria()).isEqualTo("HC-000001");
        assertThat(dto.contactos()).isEmpty();
        assertThat(dto.seguros()).isEmpty();
        assertThat(dto.antecedentes()).isEmpty();
        assertThat(dto.alergias()).isEmpty();
        assertThat(dto.estadoRegistro()).isEqualTo("ACTIVO");
    }

    @Test
    @DisplayName("RF-PAC-06: obtenerDetalleCompleto sin historia clínica devuelve campos nulos")
    void detalleCompletoSinHistoria() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(historiaRepository.findByPacienteId(1L)).thenReturn(Optional.empty());

        PacienteDetalleResponseDTO dto = service.obtenerDetalleCompleto(1L);

        assertThat(dto.numeroHistoria()).isNull();
        assertThat(dto.fechaAperturaHistoria()).isNull();
    }

    @Test
    @DisplayName("RF-PAC-06: obtenerDetalleCompleto lanza 404 si el paciente no existe")
    void detalleCompletoNoExiste() {
        when(pacienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerDetalleCompleto(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}
