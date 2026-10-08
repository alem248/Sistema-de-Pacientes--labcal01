package com.alex.paciente.controller;

import com.alex.paciente.entity.Rol;
import com.alex.paciente.service.RolService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * CRUD de roles (ADMINISTRADOR, MEDICO, RECEPCIONISTA).
 * Solo ADMINISTRADOR (URL y metodo).
 */
@Controller
@RequestMapping("/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class RolController {

    private final RolService rolService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("roles", rolService.listar());
        return "roles/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("rol", new Rol());
        model.addAttribute("esEdicion", false);
        return "roles/formulario";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("rol", rolService.obtener(id));
            model.addAttribute("esEdicion", true);
            return "roles/formulario";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/roles";
        }
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("rol") Rol rol, Model model, RedirectAttributes ra) {
        String error = validar(rol);
        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("esEdicion", rol.getId() != null);
            return "roles/formulario";
        }
        try {
            if (rol.getId() == null) {
                rolService.crear(rol);
                ra.addFlashAttribute("success", "Rol creado correctamente: " + rol.getNombre());
            } else {
                rolService.actualizar(rol.getId(), rol);
                ra.addFlashAttribute("success", "Rol actualizado correctamente: " + rol.getNombre());
            }
            return "redirect:/roles";
        } catch (Exception e) {
            model.addAttribute("error", "No se pudo guardar: " + e.getMessage());
            model.addAttribute("esEdicion", rol.getId() != null);
            return "roles/formulario";
        }
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable Long id, @RequestParam boolean activo,
                                RedirectAttributes ra) {
        try {
            Rol rol = rolService.obtener(id);
            rolService.cambiarEstado(id, activo);
            ra.addFlashAttribute("success", (activo ? "Rol activado: " : "Rol desactivado: ") + rol.getNombre());
        } catch (Exception e) {
            ra.addFlashAttribute("error", "No se pudo cambiar el estado: " + e.getMessage());
        }
        return "redirect:/roles";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            rolService.eliminar(id);
            ra.addFlashAttribute("success", "Rol eliminado correctamente");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/roles";
    }

    private String validar(Rol rol) {
        if (rol.getNombre() == null || rol.getNombre().isBlank()) {
            return "El nombre del rol es obligatorio";
        }
        rol.setNombre(rol.getNombre().trim().toUpperCase());
        if (rolService.existeNombre(rol.getNombre(), rol.getId())) {
            return "El rol '" + rol.getNombre() + "' ya existe";
        }
        if (rol.getId() == null && rolService.existeNombre(rol.getNombre(), null)) {
            return "El rol '" + rol.getNombre() + "' ya existe";
        }
        return null;
    }
}
