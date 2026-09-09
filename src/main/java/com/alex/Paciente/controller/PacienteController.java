package com.alex.Paciente.controller;

import com.alex.Paciente.entity.Paciente;
import com.alex.Paciente.exception.DocumentoDuplicadoException;
import com.alex.Paciente.repository.PacienteRepository;
import com.alex.Paciente.service.PacienteService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
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
    private final PacienteService pacienteService;

    public PacienteController(PacienteRepository pacienteRepository, PacienteService pacienteService) {
        this.pacienteRepository = pacienteRepository;
        this.pacienteService = pacienteService;
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
        // 1) Validacion en aplicacion antes de guardar
        if (pacienteService.existeDocumento(paciente.getNumeroDocumento())) {
            result.rejectValue("numeroDocumento", "error.paciente",
                    "El numero de documento ya esta registrado");
            model.addAttribute("errorDocumento", "El numero de documento '" +
                    paciente.getNumeroDocumento() + "' ya esta registrado. No se permite duplicados. (RF-PAC-02)");
            return "pacientes/formulario";
        }

        try {
            pacienteService.registrarPaciente(paciente);
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Paciente registrado correctamente. ID: " + paciente.getId() +
                    " - " + paciente.getNombreCompleto());
            return "redirect:/pacientes/nuevo?exito";
        } catch (DocumentoDuplicadoException ex) {
            result.rejectValue("numeroDocumento", "error.paciente", ex.getMessage());
            model.addAttribute("errorDocumento", ex.getMessage() + " (RF-PAC-02)");
            return "pacientes/formulario";
        } catch (DataIntegrityViolationException ex) {
            // RF-PAC-02: Evita duplicados por constraint UNIQUE en BD (numero_documento)
            result.rejectValue("numeroDocumento", "error.paciente",
                    "El numero de documento ya esta registrado");
            model.addAttribute("errorDocumento", "El numero de documento ya esta registrado. Validacion a nivel de base de datos (UNIQUE).");
            return "pacientes/formulario";
        } catch (Exception e) {
            model.addAttribute("errorGeneral", "Error al guardar: " + e.getMessage());
            return "pacientes/formulario";
        }
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/pacientes/nuevo";
    }
}
