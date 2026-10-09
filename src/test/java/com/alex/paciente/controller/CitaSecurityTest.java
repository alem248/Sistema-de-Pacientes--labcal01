package com.alex.paciente.controller;

import com.alex.paciente.repository.PacienteRepository;
import com.alex.paciente.security.RolAuthenticationSuccessHandler;
import com.alex.paciente.security.SecurityConfig;
import com.alex.paciente.service.CitaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Control de acceso al modulo de citas (Evaluacion 02).
 * ADMINISTRADOR y RECEPCIONISTA acceden; MEDICO no administra citas.
 */
@WebMvcTest(CitaController.class)
@Import({SecurityConfig.class, RolAuthenticationSuccessHandler.class})
class CitaSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CitaService citaService;

    @MockitoBean
    private PacienteRepository pacienteRepository;

    @Test
    @DisplayName("Sin sesion: /citas redirige al login")
    void sinSesionRedirigeALogin() throws Exception {
        mockMvc.perform(get("/citas"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(username = "recepcionista", roles = {"RECEPCIONISTA"})
    @DisplayName("RECEPCIONISTA puede ver /citas -> 200")
    void recepcionistaAccede() throws Exception {
        when(citaService.listar()).thenReturn(List.of());
        mockMvc.perform(get("/citas")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    @DisplayName("ADMINISTRADOR puede ver /citas -> 200")
    void administradorAccede() throws Exception {
        when(citaService.listar()).thenReturn(List.of());
        mockMvc.perform(get("/citas")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "medico", roles = {"MEDICO"})
    @DisplayName("MEDICO no administra citas -> 403")
    void medicoBloqueado() throws Exception {
        mockMvc.perform(get("/citas")).andExpect(status().isForbidden());
    }
}
