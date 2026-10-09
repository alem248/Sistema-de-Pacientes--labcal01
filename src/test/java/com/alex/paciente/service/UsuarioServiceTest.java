package com.alex.paciente.service;

import com.alex.paciente.entity.Rol;
import com.alex.paciente.entity.Usuario;
import com.alex.paciente.repository.RolRepository;
import com.alex.paciente.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias del CRUD de usuarios (Evaluacion 02 - Pregunta 3).
 * Verifican asignacion de rol, codificacion de clave y activar/desactivar.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Rol rolMedico() {
        return Rol.builder().id(2L).nombre("MEDICO").activo(true).build();
    }

    @Test
    @DisplayName("crear: asigna rol, codifica clave y guarda")
    void crear_asignaRolYCodificaClave() {
        Rol rol = rolMedico();
        when(rolRepository.findById(2L)).thenReturn(Optional.of(rol));
        when(passwordEncoder.encode("clave123")).thenReturn("HASH");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        Usuario nuevo = Usuario.builder().username("jperez").nombreCompleto("Juan Perez").build();
        Usuario guardado = usuarioService.crear(nuevo, "clave123", 2L);

        assertThat(guardado.getPassword()).isEqualTo("HASH");
        assertThat(guardado.getRol().getNombre()).isEqualTo("MEDICO");
        assertThat(guardado.getActivo()).isTrue();
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("actualizar: conserva clave si no se envia una nueva")
    void actualizar_conservaClaveSiVacia() {
        Usuario existente = Usuario.builder().id(1L).username("jperez")
                .password("HASH_VIEJO").nombreCompleto("Juan").activo(true).rol(rolMedico()).build();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(rolRepository.findById(2L)).thenReturn(Optional.of(rolMedico()));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        Usuario cambios = Usuario.builder().username("jperez2")
                .nombreCompleto("Juan Perez 2").correo("j@x.pe").build();
        Usuario resultado = usuarioService.actualizar(1L, cambios, "   ", 2L);

        assertThat(resultado.getUsername()).isEqualTo("jperez2");
        assertThat(resultado.getPassword()).isEqualTo("HASH_VIEJO");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("cambiarEstado: desactiva al usuario")
    void cambiarEstado_desactiva() {
        Usuario existente = Usuario.builder().id(1L).username("jperez").activo(true).rol(rolMedico()).build();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        Usuario resultado = usuarioService.cambiarEstado(1L, false);

        assertThat(resultado.getActivo()).isFalse();
    }

    @Test
    @DisplayName("existeUsername: detecta duplicado al crear")
    void existeUsername_duplicado() {
        when(usuarioRepository.existsByUsername("admin")).thenReturn(true);
        assertThat(usuarioService.existeUsername("admin", null)).isTrue();
    }
}
