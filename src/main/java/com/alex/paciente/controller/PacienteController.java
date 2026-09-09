package com.alex.paciente.controller;

import com.alex.paciente.entity.Antecedente;
import com.alex.paciente.entity.ContactoEmergencia;
import com.alex.paciente.entity.Paciente;
import com.alex.paciente.entity.SeguroPaciente;
import com.alex.paciente.entity.enums.*;
import com.alex.paciente.service.PacienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/pacientes")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService pacienteService;

    @Value("${paciente.upload-dir:uploads}")
    private String uploadDir;

    // ============ Listado + Búsqueda ============
    @GetMapping
    public String listar(@RequestParam(value = "q", required = false) String q,
                         @RequestParam(value = "codigo", required = false) String codigo,
                         @RequestParam(value = "documento", required = false) String documento,
                         @RequestParam(value = "nombres", required = false) String nombres,
                         @RequestParam(value = "apellidos", required = false) String apellidos,
                         @RequestParam(value = "telefono", required = false) String telefono,
                         Model model) {
        List<Paciente> pacientes;
        boolean esBusquedaAvanzada = (codigo != null && !codigo.isBlank())
                || (documento != null && !documento.isBlank())
                || (nombres != null && !nombres.isBlank())
                || (apellidos != null && !apellidos.isBlank())
                || (telefono != null && !telefono.isBlank());

        if (esBusquedaAvanzada) {
            pacientes = pacienteService.busquedaAvanzada(codigo, documento, nombres, apellidos, telefono);
            model.addAttribute("busquedaAvanzada", true);
        } else if (q != null && !q.isBlank()) {
            pacientes = pacienteService.buscar(q);
        } else {
            pacientes = pacienteService.listarTodos();
        }

        model.addAttribute("pacientes", pacientes);
        model.addAttribute("q", q);
        model.addAttribute("total", pacientes.size());
        model.addAttribute("codigo", codigo);
        model.addAttribute("documento", documento);
        model.addAttribute("nombres", nombres);
        model.addAttribute("apellidos", apellidos);
        model.addAttribute("telefono", telefono);
        return "paciente/lista";
    }

    // ============ Formulario Nuevo ============
    @GetMapping("/nuevo")
    public String nuevoForm(Model model) {
        model.addAttribute("paciente", new Paciente());
        cargarEnums(model);
        model.addAttribute("esEdicion", false);
        return "paciente/formulario";
    }

    // ============ Guardar (RF-PAC-03 + RF-PAC-04) ============
    @PostMapping
    public String guardar(@Valid @ModelAttribute("paciente") Paciente paciente,
                          BindingResult result,
                          @RequestParam(value = "foto", required = false) MultipartFile foto,
                          Model model,
                          RedirectAttributes redirect) {
        // Validar duplicado de documento antes de otras validaciones
        if (paciente.getNumeroDocumento() != null && pacienteService.existeDocumento(paciente.getNumeroDocumento())) {
            result.rejectValue("numeroDocumento", "error.paciente", "El documento " + paciente.getNumeroDocumento() + " ya está registrado. No se permiten duplicados.");
        }

        if (result.hasErrors()) {
            cargarEnums(model);
            model.addAttribute("esEdicion", false);
            return "paciente/formulario";
        }

        try {
            if (foto != null && !foto.isEmpty()) {
                String fotoUrl = guardarFoto(foto);
                paciente.setFotoUrl(fotoUrl);
            }
            Paciente guardado = pacienteService.registrarPaciente(paciente);
            redirect.addFlashAttribute("success", "Paciente registrado correctamente. Código: " + guardado.getCodigoPaciente());
            return "redirect:/pacientes/" + guardado.getId();
        } catch (IllegalArgumentException e) {
            result.rejectValue("numeroDocumento", "error.paciente", e.getMessage());
            cargarEnums(model);
            model.addAttribute("esEdicion", false);
            return "paciente/formulario";
        } catch (IOException e) {
            result.reject("foto", "Error al guardar la fotografía: " + e.getMessage());
            cargarEnums(model);
            model.addAttribute("esEdicion", false);
            return "paciente/formulario";
        }
    }

    // ============ Detalle (consulta rápida + foto + relaciones) ============
    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        return pacienteService.buscarPorId(id)
                .map(paciente -> {
                    model.addAttribute("paciente", paciente);
                    model.addAttribute("contactos", pacienteService.listarContactos(id));
                    model.addAttribute("seguros", pacienteService.listarSeguros(id));
                    model.addAttribute("antecedentes", pacienteService.listarAntecedentes(id));
                    model.addAttribute("nuevoContacto", new ContactoEmergencia());
                    model.addAttribute("nuevoSeguro", new SeguroPaciente());
                    model.addAttribute("nuevoAntecedente", new Antecedente());
                    cargarEnums(model);
                    return "paciente/detalle";
                })
                .orElseGet(() -> {
                    redirect.addFlashAttribute("error", "Paciente no encontrado");
                    return "redirect:/pacientes";
                });
    }

    // ============ Edición ============
    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        return pacienteService.buscarPorId(id)
                .map(paciente -> {
                    model.addAttribute("paciente", paciente);
                    cargarEnums(model);
                    model.addAttribute("esEdicion", true);
                    return "paciente/formulario";
                })
                .orElseGet(() -> {
                    redirect.addFlashAttribute("error", "Paciente no encontrado");
                    return "redirect:/pacientes";
                });
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("paciente") Paciente paciente,
                             BindingResult result,
                             @RequestParam(value = "foto", required = false) MultipartFile foto,
                             Model model,
                             RedirectAttributes redirect) {
        if (result.hasErrors()) {
            cargarEnums(model);
            model.addAttribute("esEdicion", true);
            return "paciente/formulario";
        }
        try {
            if (foto != null && !foto.isEmpty()) {
                String fotoUrl = guardarFoto(foto);
                paciente.setFotoUrl(fotoUrl);
            } else {
                // Mantener foto existente si no se sube nueva
                pacienteService.buscarPorId(id).ifPresent(existente -> {
                    if (existente.getFotoUrl() != null) paciente.setFotoUrl(existente.getFotoUrl());
                });
            }
            pacienteService.actualizarPaciente(id, paciente);
            redirect.addFlashAttribute("success", "Paciente actualizado correctamente");
            return "redirect:/pacientes/" + id;
        } catch (IllegalArgumentException e) {
            result.rejectValue("numeroDocumento", "error.paciente", e.getMessage());
            cargarEnums(model);
            model.addAttribute("esEdicion", true);
            return "paciente/formulario";
        } catch (IOException e) {
            result.reject("foto", "Error al guardar fotografía: " + e.getMessage());
            cargarEnums(model);
            model.addAttribute("esEdicion", true);
            return "paciente/formulario";
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Error al actualizar: " + e.getMessage());
            return "redirect:/pacientes/" + id + "/editar";
        }
    }

    // ============ Cambio de Estado (Activo / Inactivo / Fallecido) ============
    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable Long id,
                                @RequestParam("estado") EstadoRegistro estado,
                                RedirectAttributes redirect) {
        try {
            pacienteService.cambiarEstado(id, estado);
            redirect.addFlashAttribute("success", "Estado cambiado a " + estado.getDisplayName());
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pacientes/" + id;
    }

    // ============ Contacto Emergencia ============
    @PostMapping("/{id}/contactos")
    public String agregarContacto(@PathVariable Long id,
                                  @Valid @ModelAttribute("nuevoContacto") ContactoEmergencia contacto,
                                  BindingResult result,
                                  Model model,
                                  RedirectAttributes redirect) {
        if (result.hasErrors()) {
            redirect.addFlashAttribute("error", "Datos de contacto inválidos: " + result.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/pacientes/" + id;
        }
        try {
            pacienteService.agregarContacto(id, contacto);
            redirect.addFlashAttribute("success", "Contacto de emergencia agregado");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pacientes/" + id;
    }

    @PostMapping("/{pacienteId}/contactos/{contactoId}/eliminar")
    public String eliminarContacto(@PathVariable Long pacienteId,
                                   @PathVariable Long contactoId,
                                   RedirectAttributes redirect) {
        try {
            pacienteService.eliminarContacto(pacienteId, contactoId);
            redirect.addFlashAttribute("success", "Contacto eliminado");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pacientes/" + pacienteId;
    }

    // ============ Seguro ============
    @PostMapping("/{id}/seguros")
    public String agregarSeguro(@PathVariable Long id,
                                @Valid @ModelAttribute("nuevoSeguro") SeguroPaciente seguro,
                                BindingResult result,
                                RedirectAttributes redirect) {
        if (result.hasErrors()) {
            redirect.addFlashAttribute("error", "Datos de seguro inválidos");
            return "redirect:/pacientes/" + id;
        }
        try {
            pacienteService.agregarSeguro(id, seguro);
            redirect.addFlashAttribute("success", "Seguro agregado correctamente");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pacientes/" + id;
    }

    @PostMapping("/{pacienteId}/seguros/{seguroId}/eliminar")
    public String eliminarSeguro(@PathVariable Long pacienteId,
                                 @PathVariable Long seguroId,
                                 RedirectAttributes redirect) {
        try {
            pacienteService.eliminarSeguro(pacienteId, seguroId);
            redirect.addFlashAttribute("success", "Seguro eliminado");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pacientes/" + pacienteId;
    }

    // ============ Antecedentes ============
    @PostMapping("/{id}/antecedentes")
    public String agregarAntecedente(@PathVariable Long id,
                                     @Valid @ModelAttribute("nuevoAntecedente") Antecedente antecedente,
                                     BindingResult result,
                                     RedirectAttributes redirect) {
        if (result.hasErrors()) {
            redirect.addFlashAttribute("error", "Datos de antecedente inválidos");
            return "redirect:/pacientes/" + id;
        }
        try {
            pacienteService.agregarAntecedente(id, antecedente);
            redirect.addFlashAttribute("success", "Antecedente registrado");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pacientes/" + id;
    }

    @PostMapping("/{pacienteId}/antecedentes/{antecedenteId}/eliminar")
    public String eliminarAntecedente(@PathVariable Long pacienteId,
                                      @PathVariable Long antecedenteId,
                                      RedirectAttributes redirect) {
        try {
            pacienteService.eliminarAntecedente(pacienteId, antecedenteId);
            redirect.addFlashAttribute("success", "Antecedente eliminado");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pacientes/" + pacienteId;
    }

    // ============ AJAX: verificar documento (evitar duplicados) ============
    @GetMapping("/api/verificar-documento")
    @ResponseBody
    public String verificarDocumento(@RequestParam String numeroDocumento) {
        boolean existe = pacienteService.existeDocumento(numeroDocumento);
        return existe ? "EXISTE" : "DISPONIBLE";
    }

    // ============ Helpers ============
    private void cargarEnums(Model model) {
        model.addAttribute("tiposDocumento", TipoDocumento.values());
        model.addAttribute("sexos", Sexo.values());
        model.addAttribute("estadosCivil", EstadoCivil.values());
        model.addAttribute("tiposSangre", TipoSangre.values());
        model.addAttribute("estadosRegistro", EstadoRegistro.values());
        model.addAttribute("tiposSeguro", TipoSeguro.values());
        model.addAttribute("estadosCobertura", EstadoCobertura.values());
        model.addAttribute("categoriasAntecedente", CategoriaAntecedente.values());
    }

    private String guardarFoto(MultipartFile foto) throws IOException {
        Path dir = Paths.get(uploadDir);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        String original = foto.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf("."));
        }
        String filename = UUID.randomUUID() + ext;
        Path destino = dir.resolve(filename);
        Files.copy(foto.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/" + filename;
    }
}
