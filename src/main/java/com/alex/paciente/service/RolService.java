package com.alex.paciente.service;

import com.alex.paciente.audit.Auditable;
import com.alex.paciente.entity.Rol;
import com.alex.paciente.repository.RolRepository;
import com.alex.paciente.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD de roles (Evaluacion 02 - Pregunta 3/4).
 * Roles minimos: ADMINISTRADOR, MEDICO, RECEPCIONISTA.
 */
@Service
@RequiredArgsConstructor
public class RolService {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;

    public List<Rol> listar() {
        return rolRepository.findAll();
    }

    public Rol obtener(Long id) {
        return rolRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + id));
    }

    public boolean existeNombre(String nombre, Long idExcluir) {
        return rolRepository.findByNombre(nombre)
                .filter(r -> idExcluir == null || !r.getId().equals(idExcluir))
                .isPresent();
    }

    public long usuariosAsignados(Long rolId) {
        return usuarioRepository.countByRolId(rolId);
    }

    @Transactional
    @Auditable(operacion = "REGISTRO", entidad = "Rol")
    public Rol crear(Rol rol) {
        rol.setId(null);
        if (rol.getActivo() == null) rol.setActivo(true);
        return rolRepository.save(rol);
    }

    @Transactional
    @Auditable(operacion = "MODIFICACION", entidad = "Rol", idArgIndex = 0)
    public Rol actualizar(Long id, Rol datos) {
        Rol rol = obtener(id);
        rol.setNombre(datos.getNombre());
        rol.setDescripcion(datos.getDescripcion());
        rol.setActivo(datos.getActivo() != null && datos.getActivo());
        return rolRepository.save(rol);
    }

    @Transactional
    @Auditable(operacion = "MODIFICACION", entidad = "Rol", idArgIndex = 0)
    public Rol cambiarEstado(Long id, boolean activo) {
        Rol rol = obtener(id);
        rol.setActivo(activo);
        return rolRepository.save(rol);
    }

    @Transactional
    @Auditable(operacion = "ELIMINACION", entidad = "Rol", idArgIndex = 0)
    public void eliminar(Long id) {
        Rol rol = obtener(id);
        long asignados = usuarioRepository.countByRolId(id);
        if (asignados > 0) {
            throw new IllegalStateException(
                    "No se puede eliminar el rol '" + rol.getNombre() + "': tiene "
                    + asignados + " usuario(s) asignado(s)");
        }
        rolRepository.delete(rol);
    }
}
