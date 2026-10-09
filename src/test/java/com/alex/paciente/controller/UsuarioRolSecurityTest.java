package com.alex.paciente.controller;

import com.alex.paciente.entity.Rol;
import com.alex.paciente.repository.RolRepository;
import com.alex.paciente.security.RolAuthenticationSuccessHandler;
import com.alex.paciente.security.SecurityConfig;
import com.alex.paciente.service.RolService;
import com.alex.paciente.service.UsuarioService;
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
 * Pruebas de control de acceso a Usuarios y Roles (Evaluacion 02 - Pregunta 5).
 * Solo el ADMINISTRADOR puede administrarlos; el bloqueo se aplica en el backend.
 */
@WebMvcTest({UsuarioController.class, RolController.class})
@Import({SecurityConfig.class, RolAuthenticationSuccessHandler.class})
class UsuarioRolSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private RolService rolService;

    @MockitoBean
    private RolRepository rolRepository;

    @Test
    @DisplayName("Sin sesion: /usuarios redirige al login")
    void sinSesionRedirigeALogin() throws Exception {
        mockMvc.perform(get("/usuarios"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    @DisplayName("ADMINISTRADOR puede ver /usuarios y /roles -> 200")
    void administradorAccede() throws Exception {
        when(usuarioService.listar()).thenReturn(List.of());
        mockMvc.perform(get("/usuarios")).andExpect(status().isOk());

        when(rolService.listar()).thenReturn(List.of(Rol.builder().id(1L).nombre("MEDICO").build()));
        when(rolRepository.findAll()).thenReturn(List.of());
        mockMvc.perform(get("/roles")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "medico", roles = {"MEDICO"})
    @DisplayName("MEDICO no puede administrar usuarios -> 403")
    void medicoBloqueado() throws Exception {
        mockMvc.perform(get("/usuarios")).andExpect(status().isForbidden());
        mockMvc.perform(get("/roles")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "recepcionista", roles = {"RECEPCIONISTA"})
    @DisplayName("RECEPCIONISTA no puede administrar usuarios -> 403")
    void recepcionistaBloqueado() throws Exception {
        mockMvc.perform(get("/usuarios")).andExpect(status().isForbidden());
    }
}
