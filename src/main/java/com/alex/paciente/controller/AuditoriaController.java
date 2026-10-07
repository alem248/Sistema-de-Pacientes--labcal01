package com.alex.paciente.controller;

import com.alex.paciente.repository.AuditoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Consulta de la bitácora de auditoría (Pregunta 2 - "permitir consultar la bitácora").
 * Acceso restringido al rol ADMINISTRADOR (URL + método).
 */
@Controller
@RequestMapping("/auditoria")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AuditoriaController {

    private final AuditoriaRepository auditoriaRepository;

    @GetMapping
    public String bitacora(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "20") int size,
                           Model model) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        model.addAttribute("pagina", auditoriaRepository.findAllByOrderByFechaHoraDesc(pageable));
        return "auditoria/lista";
    }
}
