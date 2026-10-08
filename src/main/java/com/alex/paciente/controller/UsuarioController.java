package com.alex.paciente.controller;

import com.alex.paciente.entity.Rol;
import com.alex.paciente.entity.Usuario;
import com.alex.paciente.repository.RolRepository;
import com.alex.paciente.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * CRUD de usuarios + asignacion de roles + activar/desactivar.
 * Solo ADMINISTRADOR (URL y metodo).
 */
@Controller
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final RolRepository rolRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listar());
        return "usuarios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("roles", rolRepository.findAll());
        model.addAttribute("esEdicion", false);
        return "usuarios/formulario";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("usuario", usuarioService.obtener(id));
            model.addAttribute("roles", rolRepository.findAll());
            model.addAttribute("esEdicion", true);
            return "usuarios/formulario";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/usuarios";
        }
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("usuario") Usuario usuario,
                          @RequestParam(required = false) String password,
                          @RequestParam Long rolId,
                          Model model,
                          RedirectAttributes ra) {

        String error = validar(usuario, password, rolId, usuario.getId());
        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("roles", rolRepository.findAll());
            model.addAttribute("esEdicion", usuario.getId() != null);
            return "usuarios/formulario";
        }

        try {
            if (usuario.getId() == null) {
                usuarioService.crear(usuario, password, rolId);
                ra.addFlashAttribute("success", "Usuario registrado correctamente: " + usuario.getUsername());
            } else {
                usuarioService.actualizar(usuario.getId(), usuario, password, rolId);
                ra.addFlashAttribute("success", "Usuario actualizado correctamente: " + usuario.getUsername());
            }
            return "redirect:/usuarios";
        } catch (Exception e) {
            model.addAttribute("error", "No se pudo guardar: " + e.getMessage());
            model.addAttribute("roles", rolRepository.findAll());
            model.addAttribute("esEdicion", usuario.getId() != null);
            return "usuarios/formulario";
        }
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable Long id,
                                @RequestParam boolean activo,
                                RedirectAttributes ra) {
        try {
            String actual = SecurityContextHolder.getContext().getAuthentication().getName();
            Usuario usuario = usuarioService.obtener(id);
            if (!activo && usuario.getUsername().equals(actual)) {
                ra.addFlashAttribute("error", "No puede desactivar su propio usuario");
                return "redirect:/usuarios";
            }
            usuarioService.cambiarEstado(id, activo);
            ra.addFlashAttribute("success", (activo ? "Usuario activado: " : "Usuario desactivado: ")
                    + usuario.getUsername());
        } catch (Exception e) {
            ra.addFlashAttribute("error", "No se pudo cambiar el estado: " + e.getMessage());
        }
        return "redirect:/usuarios";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            String actual = SecurityContextHolder.getContext().getAuthentication().getName();
            Usuario usuario = usuarioService.obtener(id);
            if (usuario.getUsername().equals(actual)) {
                ra.addFlashAttribute("error", "No puede eliminar su propio usuario");
                return "redirect:/usuarios";
            }
            usuarioService.eliminar(id);
            ra.addFlashAttribute("success", "Usuario eliminado: " + usuario.getUsername());
        } catch (Exception e) {
            ra.addFlashAttribute("error", "No se pudo eliminar: " + e.getMessage());
        }
        return "redirect:/usuarios";
    }

    private String validar(Usuario usuario, String password, Long rolId, Long id) {
        if (usuario.getUsername() == null || usuario.getUsername().isBlank()) {
            return "El usuario es obligatorio";
        }
        if (usuario.getNombreCompleto() == null || usuario.getNombreCompleto().isBlank()) {
            return "El nombre completo es obligatorio";
        }
        if (rolId == null) {
            return "Debe asignar un rol";
        }
        if (id == null && (password == null || password.length() < 6)) {
            return "La clave debe tener al menos 6 caracteres";
        }
        if (password != null && !password.isBlank() && password.length() < 6) {
            return "La clave debe tener al menos 6 caracteres";
        }
        if (usuarioService.existeUsername(usuario.getUsername().trim(), id)) {
            return "El usuario '" + usuario.getUsername() + "' ya existe";
        }
        usuario.setUsername(usuario.getUsername().trim());
        return null;
    }
}
