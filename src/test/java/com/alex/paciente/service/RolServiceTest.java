package com.alex.paciente.service;

import com.alex.paciente.entity.Rol;
import com.alex.paciente.repository.RolRepository;
import com.alex.paciente.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias del CRUD de roles (Evaluacion 02 - Pregunta 3).
 */
@ExtendWith(MockitoExtension.class)
class RolServiceTest {

    @Mock
    private RolRepository rolRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private RolService rolService;

    @Test
    @DisplayName("crear: define activo por defecto y guarda")
    void crear_activoPorDefecto() {
        when(rolRepository.save(any(Rol.class))).thenAnswer(i -> i.getArgument(0));
        Rol rol = Rol.builder().nombre("RECEPCIONISTA").descripcion("Pacientes y citas").build();

        Rol guardado = rolService.crear(rol);

        assertThat(guardado.getActivo()).isTrue();
        verify(rolRepository).save(any(Rol.class));
    }

    @Test
    @DisplayName("existeNombre: evita roles duplicados")
    void existeNombre_duplicado() {
        when(rolRepository.findByNombre("MEDICO"))
                .thenReturn(Optional.of(Rol.builder().id(2L).nombre("MEDICO").build()));

        assertThat(rolService.existeNombre("MEDICO", null)).isTrue();
        assertThat(rolService.existeNombre("MEDICO", 2L)).isFalse();
    }

    @Test
    @DisplayName("eliminar: bloquea si el rol tiene usuarios asignados")
    void eliminar_conUsuariosAsignados_lanzaExcepcion() {
        when(rolRepository.findById(2L))
                .thenReturn(Optional.of(Rol.builder().id(2L).nombre("MEDICO").build()));
        when(usuarioRepository.countByRolId(2L)).thenReturn(3L);

        assertThatThrownBy(() -> rolService.eliminar(2L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("usuario");
        verify(rolRepository, never()).delete(any());
    }

    @Test
    @DisplayName("eliminar: borra cuando no tiene usuarios")
    void eliminar_sinUsuarios_borra() {
        Rol rol = Rol.builder().id(2L).nombre("MEDICO").build();
        when(rolRepository.findById(2L)).thenReturn(Optional.of(rol));
        when(usuarioRepository.countByRolId(2L)).thenReturn(0L);

        rolService.eliminar(2L);

        verify(rolRepository).delete(rol);
    }
}
