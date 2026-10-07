package com.alex.paciente.controller;

import com.alex.paciente.dto.PacienteResponseDTO;
import com.alex.paciente.exception.ResourceNotFoundException;
import com.alex.paciente.security.RolAuthenticationSuccessHandler;
import com.alex.paciente.security.SecurityConfig;
import com.alex.paciente.service.PacienteConsultaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de controlador con seguridad (Pregunta 5 - control de acceso por rol).
 * Verifica: 401 sin autenticación, 403 sin permiso, 200 con rol autorizado,
 * y la gestión de errores 400/404.
 */
@WebMvcTest(PacienteRestController.class)
@Import({SecurityConfig.class, RolAuthenticationSuccessHandler.class})
class PacienteRestControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PacienteConsultaService pacienteService;

    private PacienteResponseDTO pacienteDto() {
        return new PacienteResponseDTO(1L, "PAC-000001", "12345678", "Juan", "Pérez", "García",
                "987654321", "juan@mail.com", null, "MASCULINO", "Av. Los Pinos 123",
                true, LocalDateTime.now(), null, 0, 0);
    }

    // ---------- 401: sin autenticación ----------
    @Test
    @DisplayName("Seguridad: GET /api/v1/pacientes sin autenticación -> 401")
    void sinAutenticacion() throws Exception {
        mockMvc.perform(get("/api/v1/pacientes"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- 200: los tres roles pueden buscar (RF-PAC-05) ----------
    @Test
    @WithMockUser(username = "medico", roles = {"MEDICO"})
    @DisplayName("RF-PAC-05: MEDICO puede buscar pacientes -> 200")
    void medicoPuedeBuscar() throws Exception {
        when(pacienteService.busquedaGeneral(eq("perez"), any()))
                .thenReturn(new PageImpl<>(List.of(pacienteDto()), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/pacientes/buscar").param("q", "perez"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].dni").value("12345678"));
    }

    @Test
    @WithMockUser(username = "recepcionista", roles = {"RECEPCIONISTA"})
    @DisplayName("RF-PAC-05: RECEPCIONISTA puede buscar por DNI -> 200")
    void recepcionistaPuedeBuscarPorDni() throws Exception {
        when(pacienteService.buscarPorDni("12345678")).thenReturn(pacienteDto());

        mockMvc.perform(get("/api/v1/pacientes/dni/12345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombres").value("Juan"));
    }

    // ---------- 403: rol sin permiso ----------
    @Test
    @WithMockUser(username = "medico", roles = {"MEDICO"})
    @DisplayName("Seguridad: MEDICO no puede eliminar pacientes -> 403")
    void medicoNoPuedeEliminar() throws Exception {
        mockMvc.perform(delete("/api/v1/pacientes/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "recepcionista", roles = {"RECEPCIONISTA"})
    @DisplayName("Seguridad: RECEPCIONISTA no puede eliminar pacientes -> 403")
    void recepcionistaNoPuedeEliminar() throws Exception {
        mockMvc.perform(delete("/api/v1/pacientes/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    @DisplayName("Seguridad: ADMINISTRADOR sí puede eliminar pacientes -> 204")
    void adminPuedeEliminar() throws Exception {
        mockMvc.perform(delete("/api/v1/pacientes/1"))
                .andExpect(status().isNoContent());
    }

    // ---------- 400: validación de parámetros ----------
    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    @DisplayName("Validación: búsqueda con criterio de 1 carácter -> 400")
    void busquedaCriterioMuyCorto() throws Exception {
        mockMvc.perform(get("/api/v1/pacientes/buscar").param("q", "a"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    @DisplayName("Validación: DNI con formato inválido -> 400")
    void dniFormatoInvalido() throws Exception {
        mockMvc.perform(get("/api/v1/pacientes/dni/12"))
                .andExpect(status().isBadRequest());
    }

    // ---------- 404: paciente inexistente ----------
    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    @DisplayName("Errores: paciente inexistente -> 404 con cuerpo JSON estándar")
    void pacienteNoEncontrado() throws Exception {
        when(pacienteService.buscarPorDni("99999999"))
                .thenThrow(new ResourceNotFoundException("No se encontró paciente con DNI 99999999"));

        mockMvc.perform(get("/api/v1/pacientes/dni/99999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }
}
