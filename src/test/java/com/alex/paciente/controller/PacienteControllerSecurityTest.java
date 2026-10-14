package com.alex.paciente.controller;

import com.alex.paciente.repository.HistoriaClinicaRepository;
import com.alex.paciente.security.RolAuthenticationSuccessHandler;
import com.alex.paciente.security.SecurityConfig;
import com.alex.paciente.service.PacienteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Control de acceso del módulo WEB (MVC Thymeleaf) - Pregunta 5.
 *
 * Complementa a PacienteRestControllerSecurityTest (que cubre la API REST):
 * aquí se valida que las reglas @PreAuthorize del controlador MVC impidan
 * el acceso a funcionalidades no autorizadas aunque se escriba la URL a mano.
 */
@WebMvcTest(PacienteController.class)
@Import({SecurityConfig.class, RolAuthenticationSuccessHandler.class})
class PacienteControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PacienteService pacienteService;

    @MockitoBean
    private HistoriaClinicaRepository historiaClinicaRepository;

    // ---------- GET: los tres roles pueden consultar (RF-PAC-05/06) ----------
    @Test
    @WithMockUser(username = "medico", roles = {"MEDICO"})
    @DisplayName("MVC: MEDICO puede ver el listado de pacientes -> 200")
    void medicoPuedeVerListado() throws Exception {
        mockMvc.perform(get("/pacientes"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "medico", roles = {"MEDICO"})
    @DisplayName("RF-PAC-01: MEDICO puede abrir el formulario de registro -> 200")
    void medicoPuedeAbrirFormularioNuevo() throws Exception {
        mockMvc.perform(get("/pacientes/nuevo"))
                .andExpect(status().isOk());
    }

    // ---------- El agujero cerrado: MEDICO no puede entrar a editar por URL ----------
    @Test
    @WithMockUser(username = "medico", roles = {"MEDICO"})
    @DisplayName("Seguridad MVC: MEDICO no puede abrir /pacientes/{id}/editar -> 403")
    void medicoNoPuedeAbrirFormularioEdicion() throws Exception {
        mockMvc.perform(get("/pacientes/1/editar"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "recepcionista", roles = {"RECEPCIONISTA"})
    @DisplayName("Seguridad MVC: RECEPCIONISTA no puede registrar antecedentes -> 403")
    void recepcionistaNoPuedeAgregarAntecedente() throws Exception {
        mockMvc.perform(post("/pacientes/1/antecedentes").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "medico", roles = {"MEDICO"})
    @DisplayName("Seguridad MVC: MEDICO sí puede registrar antecedentes -> pasa la autorización")
    void medicoPuedeAgregarAntecedente() throws Exception {
        // Sin datos válidos responde 302 (redirect con error de formulario),
        // pero NUNCA 403: el rol tiene permiso y supera el @PreAuthorize.
        mockMvc.perform(post("/pacientes/1/antecedentes").with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    // ---------- Sin sesión: redirección al login ----------
    @Test
    @DisplayName("MVC: sin sesión se redirige a /login -> 302")
    void sinSesionRedirigeALogin() throws Exception {
        mockMvc.perform(get("/pacientes"))
                .andExpect(status().isFound());
    }
}
