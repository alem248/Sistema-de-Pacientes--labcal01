package com.alex.paciente.audit;

import com.alex.paciente.entity.Auditoria;
import com.alex.paciente.entity.Paciente;
import com.alex.paciente.entity.enums.Sexo;
import com.alex.paciente.repository.AuditoriaRepository;
import com.alex.paciente.service.PacienteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba de integración de la auditoría automática (Pregunta 2 - Evaluación 02).
 * Verifica que el aspecto AOP registra en la tabla auditoria: usuario autenticado,
 * fecha/hora, operación, entidad e identificador del registro afectado.
 */
@SpringBootTest
class AuditoriaIntegrationTest {

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    @DisplayName("Auditoría: registrar un paciente genera un registro REGISTRO con el usuario autenticado")
    void registraOperacionDeRegistro() {
        Paciente paciente = Paciente.builder()
                .numeroDocumento("77778888")
                .nombres("Paciente")
                .apellidoPaterno("De Prueba")
                .apellidoMaterno("Auditado")
                .fechaNacimiento(LocalDate.of(1985, 3, 10))
                .sexo(Sexo.FEMENINO)
                .telefono("999888777")
                .direccion("Calle Test 123")
                .distrito("Lima").provincia("Lima").departamento("Lima")
                .build();

        Paciente guardado = pacienteService.registrarPaciente(paciente);

        List<Auditoria> registros = auditoriaRepository
                .findByEntidadAndEntidadIdOrderByFechaHoraDesc("Paciente", guardado.getId().toString());

        assertThat(registros).isNotEmpty();
        Auditoria a = registros.get(0);
        assertThat(a.getOperacion()).isEqualTo("REGISTRO");
        assertThat(a.getUsuario()).isEqualTo("admin");
        assertThat(a.getFechaHora()).isNotNull();
        assertThat(a.getEntidad()).isEqualTo("Paciente");
        assertThat(a.getEntidadId()).isEqualTo(guardado.getId().toString());
    }

    @Test
    @WithMockUser(username = "recepcionista", roles = {"RECEPCIONISTA"})
    @DisplayName("Auditoría: eliminar lógicamente genera registros de ELIMINACION con el id afectado")
    void registraOperacionDeEliminacion() {
        Paciente guardado = pacienteService.registrarPaciente(Paciente.builder()
                .numeroDocumento("55556666")
                .nombres("Paciente")
                .apellidoPaterno("A Eliminar")
                .apellidoMaterno("Logico")
                .fechaNacimiento(LocalDate.of(1978, 7, 20))
                .sexo(Sexo.MASCULINO)
                .telefono("988877766")
                .direccion("Calle Test 456")
                .distrito("Lima").provincia("Lima").departamento("Lima")
                .build());

        pacienteService.eliminarLogico(guardado.getId());

        List<Auditoria> registros = auditoriaRepository
                .findByEntidadAndEntidadIdOrderByFechaHoraDesc("Paciente", guardado.getId().toString());

        assertThat(registros).anyMatch(a ->
                "ELIMINACION".equals(a.getOperacion()) && "recepcionista".equals(a.getUsuario()));
    }
}
