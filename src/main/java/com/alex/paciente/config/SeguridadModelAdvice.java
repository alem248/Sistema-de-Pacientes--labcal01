package com.alex.paciente.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Expone a todas las vistas Thymeleaf los datos del usuario autenticado,
 * para "mostrar únicamente las opciones permitidas" según el rol (Pregunta 5).
 */
@ControllerAdvice(annotations = org.springframework.stereotype.Controller.class)
public class SeguridadModelAdvice {

    @ModelAttribute
    public void agregarDatosSeguridad(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean autenticado = auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(String.valueOf(auth.getPrincipal()));
        model.addAttribute("autenticado", autenticado);
        if (autenticado) {
            model.addAttribute("usuarioActual", auth.getName());
            for (GrantedAuthority ga : auth.getAuthorities()) {
                String rol = ga.getAuthority(); // ROLE_ADMINISTRADOR, ...
                model.addAttribute("es" + capitalizar(rol.replace("ROLE_", "")), true);
            }
        }
    }

    private String capitalizar(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.charAt(0) + s.substring(1).toLowerCase();
    }
}
