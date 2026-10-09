package com.alex.paciente.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

/**
 * Control de acceso según el rol (Pregunta 5 - Evaluación 02).
 *
 * Matriz de acceso al módulo de pacientes (RF-PAC-05 / RF-PAC-06):
 * | Funcionalidad                    | ADMINISTRADOR | MEDICO | RECEPCIONISTA |
 * |----------------------------------|---------------|--------|---------------|
 * | Buscar pacientes (RF-PAC-05)     |       X       |   X    |       X       |
 * | Ver ficha completa (RF-PAC-06)   |       X       |   X    |       X       |
 * | Registrar / editar paciente      |       X       |        |       X       |
 * | Contactos y seguros              |       X       |        |       X       |
 * | Antecedentes / historia clínica  |       X       |   X    |               |
 * | Eliminar (API REST)              |       X       |        |               |
 * | Bitácora de auditoría            |       X       |        |               |
 *
 * La seguridad se aplica en dos capas (defensa en profundidad):
 * 1) Reglas de URL en este SecurityFilterChain.
 * 2) @PreAuthorize en los controladores/servicios (@EnableMethodSecurity).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final RolAuthenticationSuccessHandler successHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF: activo para el módulo MVC (Thymeleaf inyecta el token automáticamente
            // en los <form method="post" th:action>). Se excluye la API REST stateless,
            // pensada para clientes no-navegador autenticados con HTTP Basic.
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
            .authorizeHttpRequests(auth -> auth
                // Recursos públicos
                .requestMatchers("/login", "/acceso-denegado", "/error",
                                 "/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                // Bitácora de auditoría: solo ADMINISTRADOR
                .requestMatchers("/auditoria/**", "/api/v1/auditoria/**").hasRole("ADMINISTRADOR")
                // Usuarios y roles (CRUD + activar/desactivar): solo ADMINISTRADOR
                .requestMatchers("/usuarios/**", "/roles/**").hasRole("ADMINISTRADOR")
                // API REST de pacientes (RF-PAC-05/06)
                .requestMatchers(HttpMethod.GET, "/api/v1/pacientes/**")
                    .hasAnyRole("ADMINISTRADOR", "MEDICO", "RECEPCIONISTA")
                .requestMatchers(HttpMethod.POST, "/api/v1/pacientes/**")
                    .hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA")
                .requestMatchers(HttpMethod.PUT, "/api/v1/pacientes/**")
                    .hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/pacientes/**")
                    .hasRole("ADMINISTRADOR")
                // API clínica (alergias, antecedentes, historia): ADMINISTRADOR y MEDICO
                .requestMatchers("/api/v1/alergias/**", "/api/v1/antecedentes/**")
                    .hasAnyRole("ADMINISTRADOR", "MEDICO")
                // MVC pacientes: lectura para los tres roles
                .requestMatchers(HttpMethod.GET, "/pacientes", "/pacientes/{id:\\d+}",
                                 "/pacientes/api/verificar-documento")
                    .hasAnyRole("ADMINISTRADOR", "MEDICO", "RECEPCIONISTA")
                // MVC pacientes: cualquier otra operación (registro, edición, estado,
                // contactos, seguros, antecedentes) requiere rol operativo
                .requestMatchers("/pacientes/**")
                    .hasAnyRole("ADMINISTRADOR", "MEDICO", "RECEPCIONISTA")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(successHandler) // redirección según rol
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .httpBasic(basic -> {}) // autenticación para la API REST (Postman, JS)
            .exceptionHandling(ex -> ex
                // API REST: 401 JSON cuando no hay autenticación
                .defaultAuthenticationEntryPointFor((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write("{\"status\":401,\"error\":\"No autenticado\","
                            + "\"message\":\"Debe iniciar sesión para acceder a este recurso\"}");
                }, PathPatternRequestMatcher.withDefaults().matcher("/api/**"))
                // API REST: 403 JSON cuando está autenticado pero sin permiso
                .defaultAccessDeniedHandlerFor((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write("{\"status\":403,\"error\":\"Acceso denegado\","
                            + "\"message\":\"Su rol no tiene permiso para esta operación\"}");
                }, PathPatternRequestMatcher.withDefaults().matcher("/api/**"))
                // MVC: página de acceso denegado
                .accessDeniedPage("/acceso-denegado")
                // Modulo web: los accesos sin sesion se redirigen al formulario de login
                .defaultAuthenticationEntryPointFor(
                        new org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint("/login"),
                        PathPatternRequestMatcher.withDefaults().matcher("/**"))
            );
        return http.build();
    }
}
