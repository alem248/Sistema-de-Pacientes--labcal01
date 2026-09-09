package com.alex.Paciente.controller;

import com.alex.Paciente.entity.Paciente;
import com.alex.Paciente.repository.PacienteRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * RF-PAC-01: Registro de pacientes
 * RF-PAC-02: Validacion del numero de documento
 */
@Controller
@RequestMapping("/pacientes")
public class PacienteController {

    private final PacienteRepository pacienteRepository;

    public PacienteController(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("paciente", new Paciente());
        return "pacientes/formulario";
    }

    @GetMapping
    public String listarPacientes(Model model) {
        model.addAttribute("pacientes", pacienteRepository.findAll());
        return "pacientes/lista";
    }

    @PostMapping("/guardar")
    public String guardarPaciente(
            @Valid @ModelAttribute("paciente") Paciente paciente,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Validar errores de campos obligatorios
        if (result.hasErrors()) {
            return "pacientes/formulario";
        }

        // RF-PAC-02: Validacion del numero de documento - no permitir duplicados
        if (pacienteRepository.existsByNumeroDocumento(paciente.getNumeroDocumento())) {
            result.rejectValue("numeroDocumento", "error.paciente",
                    "El numero de documento ya esta registrado");
            model.addAttribute("errorDocumento", "El numero de documento '" +
                    paciente.getNumeroDocumento() + "' ya esta registrado. No se permite duplicados.");
            return "pacientes/formulario";
        }

        try {
            pacienteRepository.save(paciente);
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Paciente registrado correctamente. ID: " + paciente.getId() +
                    " - " + paciente.getNombreCompleto());
            return "redirect:/pacientes/nuevo?exito";
        } catch (Exception e) {
            // Captura excepcion por constraint unico en BD (race condition)
            if (e.getMessage() != null && e.getMessage().contains("uk_numero_documento")) {
                result.rejectValue("numeroDocumento", "error.paciente",
                        "El numero de documento ya esta registrado");
                model.addAttribute("errorDocumento", "El numero de documento ya esta registrado (validacion BD).");
                return "pacientes/formulario";
            }
            model.addAttribute("errorGeneral", "Error al guardar: " + e.getMessage());
            return "pacientes/formulario";
        }
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/pacientes/nuevo";
    }
}
