package com.alex.paciente.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Redirección después del inicio de sesión según el rol (Pregunta 5 - Evaluación 02):
 * - ADMINISTRADOR  -> /auditoria (bitácora del sistema)
 * - MEDICO         -> /pacientes (pacientes e historias clínicas)
 * - RECEPCIONISTA  -> /pacientes (pacientes y citas)
 */
@Component
public class RolAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String destino = "/pacientes";
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if ("ROLE_ADMINISTRADOR".equals(authority.getAuthority())) {
                destino = "/auditoria";
                break;
            }
        }
        response.sendRedirect(request.getContextPath() + destino);
    }
}
