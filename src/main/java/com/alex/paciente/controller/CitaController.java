package com.alex.paciente.controller;

import com.alex.paciente.entity.Cita;
import com.alex.paciente.repository.PacienteRepository;
import com.alex.paciente.service.CitaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

/**
 * Modulo de citas (Evaluacion 02 - rol RECEPCIONISTA, tambien ADMINISTRADOR).
 */
@Controller
@RequestMapping("/citas")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA')")
public class CitaController {

    private final CitaService citaService;
    private final PacienteRepository pacienteRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("citas", citaService.listar());
        return "citas/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("cita", new Cita());
        model.addAttribute("pacientes", pacienteRepository.findAll());
        model.addAttribute("estados", Cita.EstadoCita.values());
        return "citas/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("cita") Cita cita,
                          @RequestParam(required = false) Long pacienteId,
                          Model model,
                          RedirectAttributes ra) {
        if (pacienteId == null) {
            model.addAttribute("error", "Debe seleccionar un paciente");
            model.addAttribute("pacientes", pacienteRepository.findAll());
            model.addAttribute("estados", Cita.EstadoCita.values());
            return "citas/formulario";
        }
        if (cita.getMotivo() == null || cita.getMotivo().isBlank()) {
            model.addAttribute("error", "El motivo es obligatorio");
            model.addAttribute("pacientes", pacienteRepository.findAll());
            model.addAttribute("estados", Cita.EstadoCita.values());
            return "citas/formulario";
        }
        if (cita.getFechaHora() == null) {
            model.addAttribute("error", "La fecha y hora son obligatorias");
            model.addAttribute("pacientes", pacienteRepository.findAll());
            model.addAttribute("estados", Cita.EstadoCita.values());
            return "citas/formulario";
        }
        try {
            citaService.crear(cita, pacienteId);
            ra.addFlashAttribute("success", "Cita registrada correctamente");
            return "redirect:/citas";
        } catch (Exception e) {
            model.addAttribute("error", "No se pudo registrar: " + e.getMessage());
            model.addAttribute("pacientes", pacienteRepository.findAll());
            model.addAttribute("estados", Cita.EstadoCita.values());
            return "citas/formulario";
        }
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable Long id,
                                @RequestParam Cita.EstadoCita estado,
                                RedirectAttributes ra) {
        try {
            citaService.cambiarEstado(id, estado);
            ra.addFlashAttribute("success", "Estado de la cita actualizado a " + estado);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "No se pudo actualizar: " + e.getMessage());
        }
        return "redirect:/citas";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            citaService.eliminar(id);
            ra.addFlashAttribute("success", "Cita eliminada correctamente");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "No se pudo eliminar: " + e.getMessage());
        }
        return "redirect:/citas";
    }
}
