package com.alex.paciente.service;

import com.alex.paciente.dto.AntecedenteRequestDTO;
import com.alex.paciente.dto.AntecedenteResponseDTO;
import com.alex.paciente.entity.Antecedente;
import com.alex.paciente.entity.Paciente;
import com.alex.paciente.enums.CategoriaAntecedente;
import com.alex.paciente.enums.TipoAntecedente;
import com.alex.paciente.exception.ResourceNotFoundException;
import com.alex.paciente.repository.AntecedenteRepository;
import com.alex.paciente.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Req.5 - Lógica de antecedentes personales/familiares.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AntecedenteServiceImpl implements AntecedenteService {

    private final AntecedenteRepository antecedenteRepository;
    private final PacienteRepository pacienteRepository;

    @Override
    public AntecedenteResponseDTO crear(Long pacienteId, AntecedenteRequestDTO dto) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró paciente con id " + pacienteId));
        Antecedente a = Antecedente.builder()
                // Modelo companero (MVC Thymeleaf)
                .categoria(mapToCategoriaCompanero(dto.tipo()))
                .tipo(dto.categoria().name())
                .descripcion(dto.descripcion().trim())
                // Req.05 modelo tipado REST
                .tipoAntecedente(dto.tipo())
                .categoriaDetalle(dto.categoria())
                .fechaDiagnostico(dto.fechaDiagnostico())
                .activo(true)
                .paciente(paciente)
                .build();
        return toDTO(antecedenteRepository.save(a));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AntecedenteResponseDTO> listarPorPaciente(Long pacienteId) {
        verificarPaciente(pacienteId);
        return antecedenteRepository.findByPacienteId(pacienteId).stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AntecedenteResponseDTO> listarPorPacienteYTipo(Long pacienteId, TipoAntecedente tipo) {
        verificarPaciente(pacienteId);
        return antecedenteRepository.findByPacienteIdAndTipoAntecedente(pacienteId, tipo).stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AntecedenteResponseDTO> listarPorPacienteYCategoria(Long pacienteId, CategoriaAntecedente categoria) {
        verificarPaciente(pacienteId);
        return antecedenteRepository.findByPacienteIdAndCategoriaDetalle(pacienteId, categoria).stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AntecedenteResponseDTO obtenerPorId(Long id) {
        return toDTO(getEntity(id));
    }

    @Override
    public AntecedenteResponseDTO actualizar(Long id, AntecedenteRequestDTO dto) {
        Antecedente a = getEntity(id);
        a.setCategoria(mapToCategoriaCompanero(dto.tipo()));
        a.setTipo(dto.categoria().name());
        a.setTipoAntecedente(dto.tipo());
        a.setCategoriaDetalle(dto.categoria());
        a.setDescripcion(dto.descripcion().trim());
        a.setFechaDiagnostico(dto.fechaDiagnostico());
        return toDTO(antecedenteRepository.save(a));
    }

    @Override
    public void eliminar(Long id) {
        antecedenteRepository.delete(getEntity(id));
    }

    private Antecedente getEntity(Long id) {
        return antecedenteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró antecedente con id " + id));
    }

    private void verificarPaciente(Long pacienteId) {
        if (!pacienteRepository.existsById(pacienteId))
            throw new ResourceNotFoundException("No se encontró paciente con id " + pacienteId);
    }

    private AntecedenteResponseDTO toDTO(Antecedente a) {
        Paciente p = a.getPaciente();
        String nombre = p.getNombres() + " " + p.getApellidoPaterno();
        // Registros creados por MVC solo tienen categoria/tipo String; mapear a enums REST con fallback.
        TipoAntecedente tipo = a.getTipoAntecedente() != null
                ? a.getTipoAntecedente()
                : mapToTipoRest(a.getCategoria());
        CategoriaAntecedente categoria = a.getCategoriaDetalle() != null
                ? a.getCategoriaDetalle()
                : mapToCategoriaRest(a.getTipo());
        return new AntecedenteResponseDTO(
                a.getId(), tipo, categoria, a.getDescripcion(),
                a.getFechaDiagnostico(), a.getFechaRegistro(), a.getActivo(),
                p.getId(), nombre);
    }

    private com.alex.paciente.entity.enums.CategoriaAntecedente mapToCategoriaCompanero(TipoAntecedente tipo) {
        if (tipo == null) return com.alex.paciente.entity.enums.CategoriaAntecedente.PERSONAL;
        return tipo == TipoAntecedente.FAMILIAR
                ? com.alex.paciente.entity.enums.CategoriaAntecedente.FAMILIAR
                : com.alex.paciente.entity.enums.CategoriaAntecedente.PERSONAL;
    }

    private TipoAntecedente mapToTipoRest(com.alex.paciente.entity.enums.CategoriaAntecedente categoria) {
        if (categoria == com.alex.paciente.entity.enums.CategoriaAntecedente.FAMILIAR) return TipoAntecedente.FAMILIAR;
        return TipoAntecedente.PERSONAL;
    }

    private CategoriaAntecedente mapToCategoriaRest(String tipoStr) {
        if (tipoStr == null) return CategoriaAntecedente.OTRO;
        try {
            return CategoriaAntecedente.valueOf(tipoStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return CategoriaAntecedente.OTRO;
        }
    }
}
