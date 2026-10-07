package com.alex.paciente.security;

import com.alex.paciente.entity.Usuario;
import com.alex.paciente.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Carga el usuario desde la BD para Spring Security.
 * El rol se expone con prefijo ROLE_ (convención de hasRole()).
 */
@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .filter(u -> u.getRol() != null && Boolean.TRUE.equals(u.getRol().getActivo()))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado o rol inactivo: " + username));

        // Registrar último acceso (auditoría de autenticación)
        usuario.setUltimoAcceso(LocalDateTime.now());
        usuarioRepository.save(usuario);

        return new User(
                usuario.getUsername(),
                usuario.getPassword(),
                Boolean.TRUE.equals(usuario.getActivo()), // enabled
                true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getNombre()))
        );
    }
}
