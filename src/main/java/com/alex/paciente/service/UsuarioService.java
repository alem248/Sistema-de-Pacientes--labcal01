package com.alex.paciente.service;

import com.alex.paciente.audit.Auditable;
import com.alex.paciente.entity.Rol;
import com.alex.paciente.entity.Usuario;
import com.alex.paciente.repository.RolRepository;
import com.alex.paciente.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD de usuarios (Evaluacion 02 - Pregunta 3/4).
 * Todas las operaciones quedan registradas en la bitacora via @Auditable.
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    public Usuario obtener(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
    }

    public boolean existeUsername(String username, Long idExcluir) {
        if (idExcluir == null) {
            return usuarioRepository.existsByUsername(username);
        }
        return usuarioRepository.findByUsername(username)
                .filter(u -> !u.getId().equals(idExcluir))
                .isPresent();
    }

    @Transactional
    @Auditable(operacion = "REGISTRO", entidad = "Usuario")
    public Usuario crear(Usuario usuario, String passwordPlano, Long rolId) {
        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));
        usuario.setId(null);
        usuario.setPassword(passwordEncoder.encode(passwordPlano));
        usuario.setRol(rol);
        usuario.setActivo(true);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    @Auditable(operacion = "MODIFICACION", entidad = "Usuario", idArgIndex = 0)
    public Usuario actualizar(Long id, Usuario datos, String passwordPlano, Long rolId) {
        Usuario usuario = obtener(id);
        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));
        usuario.setUsername(datos.getUsername());
        usuario.setNombreCompleto(datos.getNombreCompleto());
        usuario.setCorreo(datos.getCorreo());
        usuario.setActivo(datos.getActivo() != null && datos.getActivo());
        usuario.setRol(rol);
        if (passwordPlano != null && !passwordPlano.isBlank()) {
            usuario.setPassword(passwordEncoder.encode(passwordPlano));
        }
        return usuarioRepository.save(usuario);
    }

    /**
     * Activa o desactiva un usuario. Un usuario desactivado no puede iniciar sesion
     * (Spring Security lo bloquea porque enabled = false).
     */
    @Transactional
    @Auditable(operacion = "MODIFICACION", entidad = "Usuario", idArgIndex = 0)
    public Usuario cambiarEstado(Long id, boolean activo) {
        Usuario usuario = obtener(id);
        usuario.setActivo(activo);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    @Auditable(operacion = "ELIMINACION", entidad = "Usuario", idArgIndex = 0)
    public void eliminar(Long id) {
        usuarioRepository.delete(obtener(id));
    }
}
