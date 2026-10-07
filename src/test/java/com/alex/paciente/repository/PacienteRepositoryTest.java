package com.alex.paciente.repository;

import com.alex.paciente.entity.Paciente;
import com.alex.paciente.entity.enums.EstadoRegistro;
import com.alex.paciente.entity.enums.Sexo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas de capa de persistencia para RF-PAC-05 (búsqueda de pacientes).
 * Usa H2 en memoria: no requiere MySQL.
 */
@DataJpaTest
class PacienteRepositoryTest {

    @Autowired
    private PacienteRepository pacienteRepository;

    @BeforeEach
    void setUp() {
        pacienteRepository.deleteAll();
        pacienteRepository.save(paciente("PAC-000001", "12345678", "Juan Carlos", "Pérez", "García"));
        pacienteRepository.save(paciente("PAC-000002", "87654321", "María", "Quispe", "Lopez"));
        pacienteRepository.save(paciente("PAC-000003", "45678912", "Pedro", "Pérez", "Ramos"));
    }

    private Paciente paciente(String codigo, String dni, String nombres, String apPaterno, String apMaterno) {
        return Paciente.builder()
                .codigoPaciente(codigo)
                .numeroDocumento(dni)
                .nombres(nombres)
                .apellidoPaterno(apPaterno)
                .apellidoMaterno(apMaterno)
                .fechaNacimiento(LocalDate.of(1990, 5, 15))
                .sexo(Sexo.MASCULINO)
                .telefono("987654321")
                .direccion("Av. Los Pinos 123")
                .distrito("Lima").provincia("Lima").departamento("Lima")
                .estadoRegistro(EstadoRegistro.ACTIVO)
                .build();
    }

    // ---------- RF-PAC-05: búsqueda por documento ----------
    @Test
    @DisplayName("RF-PAC-05: buscar por número de documento exacto")
    void buscarPorDocumento() {
        Optional<Paciente> encontrado = pacienteRepository.findByNumeroDocumento("12345678");
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNombres()).isEqualTo("Juan Carlos");
    }

    // ---------- RF-PAC-05: búsqueda por código ----------
    @Test
    @DisplayName("RF-PAC-05: buscar por código de paciente")
    void buscarPorCodigo() {
        Optional<Paciente> encontrado = pacienteRepository.findByCodigoPaciente("PAC-000002");
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getApellidoPaterno()).isEqualTo("Quispe");
    }

    // ---------- RF-PAC-05: búsqueda por nombres ----------
    @Test
    @DisplayName("RF-PAC-05: búsqueda general encuentra por nombre (case-insensitive)")
    void buscarGeneralPorNombre() {
        List<Paciente> resultados = pacienteRepository.buscarGeneral("maría");
        assertThat(resultados).hasSize(1);
        assertThat(resultados.get(0).getNumeroDocumento()).isEqualTo("87654321");
    }

    // ---------- RF-PAC-05: búsqueda por apellidos ----------
    @Test
    @DisplayName("RF-PAC-05: búsqueda general encuentra varios por apellido paterno")
    void buscarGeneralPorApellido() {
        List<Paciente> resultados = pacienteRepository.buscarGeneral("Pérez");
        assertThat(resultados).hasSize(2);
    }

    @Test
    @DisplayName("RF-PAC-05: búsqueda general encuentra por apellido materno")
    void buscarGeneralPorApellidoMaterno() {
        List<Paciente> resultados = pacienteRepository.buscarGeneral("Lopez");
        assertThat(resultados).hasSize(1);
        assertThat(resultados.get(0).getCodigoPaciente()).isEqualTo("PAC-000002");
    }

    // ---------- RF-PAC-05: búsqueda avanzada combinada ----------
    @Test
    @DisplayName("RF-PAC-05: búsqueda avanzada combina apellidos y nombres")
    void busquedaAvanzadaCombinada() {
        List<Paciente> resultados = pacienteRepository.buscarAvanzada(null, null, "juan", "pérez", null);
        assertThat(resultados).hasSize(1);
        assertThat(resultados.get(0).getNumeroDocumento()).isEqualTo("12345678");
    }

    @Test
    @DisplayName("RF-PAC-05: búsqueda avanzada solo por documento")
    void busquedaAvanzadaPorDocumento() {
        List<Paciente> resultados = pacienteRepository.buscarAvanzada(null, "45678912", null, null, null);
        assertThat(resultados).hasSize(1);
        assertThat(resultados.get(0).getNombres()).isEqualTo("Pedro");
    }

    // ---------- RF-PAC-05: búsqueda general paginada (API REST) ----------
    @Test
    @DisplayName("RF-PAC-05: búsqueda general paginada por apellido")
    void busquedaGeneralPaginada() {
        Page<Paciente> pagina = pacienteRepository.busquedaGeneral("pérez", PageRequest.of(0, 10));
        assertThat(pagina.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("RF-PAC-05: búsqueda sin resultados devuelve lista vacía (no error)")
    void busquedaSinResultados() {
        List<Paciente> resultados = pacienteRepository.buscarGeneral("zzz-no-existe");
        assertThat(resultados).isEmpty();
    }

    // ---------- Unicidad (validación de duplicados) ----------
    @Test
    @DisplayName("existsByNumeroDocumento detecta duplicados")
    void existeDocumento() {
        assertThat(pacienteRepository.existsByNumeroDocumento("12345678")).isTrue();
        assertThat(pacienteRepository.existsByNumeroDocumento("00000000")).isFalse();
    }
}
