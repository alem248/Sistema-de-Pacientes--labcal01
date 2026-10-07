package com.alex.paciente.security;

import com.alex.paciente.entity.Rol;
import com.alex.paciente.entity.Usuario;
import com.alex.paciente.repository.RolRepository;
import com.alex.paciente.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Carga inicial de roles y usuarios (idempotente).
 *
 * Usuarios de demostración (cambiar en producción):
 * | Usuario      | Contraseña   | Rol           |
 * |--------------|--------------|---------------|
 * | admin        | admin123     | ADMINISTRADOR |
 * | medico       | medico123    | MEDICO        |
 * | recepcionista| recep123     | RECEPCIONISTA |
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DatosSeguridadSeeder implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        Rol admin = crearRolSiNoExiste("ADMINISTRADOR", "Acceso total: usuarios, roles, auditoría y módulos del sistema");
        Rol medico = crearRolSiNoExiste("MEDICO", "Acceso a pacientes e historias clínicas");
        Rol recep = crearRolSiNoExiste("RECEPCIONISTA", "Acceso a pacientes y citas");

        crearUsuarioSiNoExiste("admin", "admin123", "Administrador del Sistema", "admin@clinica.pe", admin);
        crearUsuarioSiNoExiste("medico", "medico123", "Dra./Dr. Médico", "medico@clinica.pe", medico);
        crearUsuarioSiNoExiste("recepcionista", "recep123", "Recepcionista de Sede", "recepcion@clinica.pe", recep);
    }

    private Rol crearRolSiNoExiste(String nombre, String descripcion) {
        return rolRepository.findByNombre(nombre).orElseGet(() -> {
            log.info("[SEED] Creando rol {}", nombre);
            return rolRepository.save(Rol.builder().nombre(nombre).descripcion(descripcion).activo(true).build());
        });
    }

    private void crearUsuarioSiNoExiste(String username, String clave, String nombreCompleto, String correo, Rol rol) {
        if (!usuarioRepository.existsByUsername(username)) {
            log.info("[SEED] Creando usuario {} con rol {}", username, rol.getNombre());
            usuarioRepository.save(Usuario.builder()
                    .username(username)
                    .password(passwordEncoder.encode(clave))
                    .nombreCompleto(nombreCompleto)
                    .correo(correo)
                    .activo(true)
                    .rol(rol)
                    .build());
        }
    }
}
