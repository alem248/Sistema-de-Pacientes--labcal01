package com.alex.paciente.service;

import com.alex.paciente.dto.*;
import com.alex.paciente.entity.HistoriaClinica;
import com.alex.paciente.entity.Paciente;
import com.alex.paciente.entity.enums.EstadoRegistro;
import com.alex.paciente.entity.enums.Sexo;
import com.alex.paciente.exception.DuplicateResourceException;
import com.alex.paciente.exception.ResourceNotFoundException;
import com.alex.paciente.repository.HistoriaClinicaRepository;
import com.alex.paciente.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * L├│gica de negocio de Paciente + Req.6 b├║squeda.
 * IoC/DI por constructor (RequiredArgsConstructor).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PacienteConsultaServiceImpl implements PacienteConsultaService {

    private final PacienteRepository pacienteRepository;
    private final HistoriaClinicaRepository historiaRepository;

    @Override
    public PacienteResponseDTO crear(PacienteRequestDTO dto) {
        if (pacienteRepository.existsByDni(dto.dni()))
            throw new DuplicateResourceException("Ya existe un paciente con DNI " + dto.dni());
        if (pacienteRepository.existsByCodigo(dto.codigo()))
            throw new DuplicateResourceException("Ya existe un paciente con c├│digo " + dto.codigo());
        Paciente p = toEntity(dto);
        return toDTO(pacienteRepository.save(p));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PacienteResponseDTO> listarTodos() {
        return pacienteRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PacienteResponseDTO> listarPaginado(Pageable pageable) {
        return pacienteRepository.findAll(pageable).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public PacienteResponseDTO obtenerPorId(Long id) {
        return toDTO(getEntity(id));
    }

    @Override
    public PacienteResponseDTO actualizar(Long id, PacienteRequestDTO dto) {
        Paciente p = getEntity(id);
        if (!p.getDni().equals(dto.dni()) && pacienteRepository.existsByDni(dto.dni()))
            throw new DuplicateResourceException("Ya existe un paciente con DNI " + dto.dni());
        if (!p.getCodigo().equals(dto.codigo()) && pacienteRepository.existsByCodigo(dto.codigo()))
            throw new DuplicateResourceException("Ya existe un paciente con c├│digo " + dto.codigo());
        p.setCodigo(dto.codigo());
        p.setDni(dto.dni());
        p.setNombres(dto.nombres());
        p.setApellidoPaterno(dto.apellidoPaterno());
        p.setApellidoMaterno(dto.apellidoMaterno());
        p.setTelefono(dto.telefono());
        p.setEmail(dto.email());
        p.setFechaNacimiento(dto.fechaNacimiento());
        p.setSexo(parseSexo(dto.sexo()));
        p.setDireccion(dto.direccion());
        return toDTO(pacienteRepository.save(p));
    }

    @Override
    public void eliminar(Long id) {
        Paciente p = getEntity(id);
        pacienteRepository.delete(p);
    }

    // ---------- Req.6 ----------
    @Override
    @Transactional(readOnly = true)
    public PacienteResponseDTO buscarPorDni(String dni) {
        Paciente p = pacienteRepository.findByDni(dni)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontr├│ paciente con DNI " + dni));
        return toDTO(p);
    }

    @Override
    @Transactional(readOnly = true)
    public PacienteResponseDTO buscarPorCodigo(String codigo) {
        Paciente p = pacienteRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontr├│ paciente con c├│digo " + codigo));
        return toDTO(p);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PacienteResponseDTO> buscarPorNombres(String nombres) {
        return pacienteRepository.findByNombresContainingIgnoreCase(nombres).stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PacienteResponseDTO> buscarPorApellidos(String apellidos) {
        return pacienteRepository.buscarPorApellidos(apellidos).stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PacienteResponseDTO> buscarPorTelefono(String telefono) {
        return pacienteRepository.findByTelefonoContaining(telefono).stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PacienteResponseDTO buscarPorHistoria(String numeroHistoria) {
        Paciente p = pacienteRepository.findByHistoriaClinica_NumeroHistoria(numeroHistoria)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontr├│ paciente con historia " + numeroHistoria));
        return toDTO(p);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PacienteResponseDTO> busquedaAvanzada(String dni, String codigo, String nombres,
                                                     String apellidos, String telefono,
                                                     String numeroHistoria, Pageable pageable) {
        return pacienteRepository.busquedaAvanzada(
                emptyToNull(dni), emptyToNull(codigo), emptyToNull(nombres),
                emptyToNull(apellidos), emptyToNull(telefono), emptyToNull(numeroHistoria), pageable)
                .map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PacienteResponseDTO> busquedaGeneral(String q, Pageable pageable) {
        if (q == null || q.isBlank()) return pacienteRepository.findAll(pageable).map(this::toDTO);
        return pacienteRepository.busquedaGeneral(q.trim(), pageable).map(this::toDTO);
    }

    // ---------- helpers ----------
    private Paciente getEntity(Long id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontr├│ paciente con id " + id));
    }

    private Paciente toEntity(PacienteRequestDTO dto) {
        return Paciente.builder()
                .codigoPaciente(dto.codigo().trim())
                .numeroDocumento(dto.dni().trim())
                .nombres(dto.nombres().trim())
                .apellidoPaterno(dto.apellidoPaterno().trim())
                .apellidoMaterno(dto.apellidoMaterno() == null ? null : dto.apellidoMaterno().trim())
                .telefono(dto.telefono())
                .correo(dto.email())
                .fechaNacimiento(dto.fechaNacimiento())
                .sexo(parseSexo(dto.sexo()))
                .direccion(dto.direccion())
                .estadoRegistro(EstadoRegistro.ACTIVO)
                .build();
    }

    private PacienteResponseDTO toDTO(Paciente p) {
        HistoriaClinica h = p.getHistoriaClinica();
        // Evita LazyInitialization: si historia es proxy sin inicializar y open-in-view=false,
        // se resuelve v├¡a repositorio.
        String numeroHistoria = null;
        if (h != null) {
            try {
                numeroHistoria = h.getNumeroHistoria();
            } catch (Exception ignored) {
                numeroHistoria = historiaRepository.findByPacienteId(p.getId())
                        .map(HistoriaClinica::getNumeroHistoria).orElse(null);
            }
        } else {
            numeroHistoria = historiaRepository.findByPacienteId(p.getId())
                    .map(HistoriaClinica::getNumeroHistoria).orElse(null);
        }
        int totalAnt = p.getAntecedentes() == null ? 0 : p.getAntecedentes().size();
        int totalAle = p.getPacienteAlergias() == null ? 0 : p.getPacienteAlergias().size();
        String sexoStr = p.getSexo() == null ? null : p.getSexo().name();
        return new PacienteResponseDTO(
                p.getId(), p.getCodigo(), p.getDni(), p.getNombres(),
                p.getApellidoPaterno(), p.getApellidoMaterno(), p.getTelefono(),
                p.getEmail(), p.getFechaNacimiento(), sexoStr, p.getDireccion(),
                p.getActivo(), p.getFechaRegistro(), numeroHistoria, totalAnt, totalAle);
    }

    private Sexo parseSexo(String s) {
        if (s == null || s.isBlank()) return Sexo.OTRO;
        String v = s.trim().toUpperCase();
        return switch (v) {
            case "M", "MASCULINO" -> Sexo.MASCULINO;
            case "F", "FEMENINO" -> Sexo.FEMENINO;
            default -> Sexo.OTRO;
        };
    }

    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
