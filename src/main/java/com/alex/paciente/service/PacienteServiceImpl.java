package com.alex.paciente.service;

import com.alex.paciente.entity.Antecedente;
import com.alex.paciente.entity.ContactoEmergencia;
import com.alex.paciente.entity.Paciente;
import com.alex.paciente.entity.SeguroPaciente;
import com.alex.paciente.entity.enums.EstadoRegistro;
import com.alex.paciente.repository.AntecedenteRepository;
import com.alex.paciente.repository.ContactoEmergenciaRepository;
import com.alex.paciente.repository.PacienteRepository;
import com.alex.paciente.repository.SeguroPacienteRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PacienteServiceImpl implements PacienteService {

    private final PacienteRepository pacienteRepository;
    private final ContactoEmergenciaRepository contactoRepository;
    private final SeguroPacienteRepository seguroRepository;
    private final AntecedenteRepository antecedenteRepository;

    // ================= RF-PAC-03: Código único automático =================
    @Override
    public synchronized String generarCodigoPaciente() {
        // Formato: PAC-000001, incremental basado en MAX id
        Optional<Paciente> ultimo = pacienteRepository.findTopByOrderByIdDesc();
        long siguiente = 1;
        if (ultimo.isPresent()) {
            String ultimoCodigo = ultimo.get().getCodigoPaciente();
            try {
                // Esperado PAC-000001 -> extraer numero
                String numeroStr = ultimoCodigo.replace("PAC-", "").trim();
                siguiente = Long.parseLong(numeroStr) + 1;
            } catch (NumberFormatException e) {
                // Fallback: usar id + 1
                siguiente = ultimo.get().getId() + 1;
            }
            // Evitar colisiones si hay huecos
            while (pacienteRepository.existsByCodigoPaciente(String.format("PAC-%06d", siguiente))) {
                siguiente++;
            }
        }
        return String.format("PAC-%06d", siguiente);
    }

    // ================= RF-PAC-04: Registro con validaciones =================
    @Override
    public Paciente registrarPaciente(Paciente paciente) {
        // Verificar que el documento no esté registrado previamente (identificación)
        if (paciente.getNumeroDocumento() != null && existeDocumento(paciente.getNumeroDocumento())) {
            throw new IllegalArgumentException("El documento " + paciente.getNumeroDocumento() + " ya está registrado. Se evita duplicado.");
        }
        // Generar código automático si no viene
        if (paciente.getCodigoPaciente() == null || paciente.getCodigoPaciente().isBlank()) {
            paciente.setCodigoPaciente(generarCodigoPaciente());
        } else {
            // Verificar código no duplicado
            if (pacienteRepository.existsByCodigoPaciente(paciente.getCodigoPaciente())) {
                throw new IllegalArgumentException("El código de paciente " + paciente.getCodigoPaciente() + " ya existe.");
            }
        }
        // Asegurar relaciones bidireccionales si vienen en el builder
        if (paciente.getContactos() != null) {
            paciente.getContactos().forEach(c -> c.setPaciente(paciente));
        }
        if (paciente.getSeguros() != null) {
            paciente.getSeguros().forEach(s -> s.setPaciente(paciente));
        }
        if (paciente.getAntecedentes() != null) {
            paciente.getAntecedentes().forEach(a -> a.setPaciente(paciente));
        }
        return pacienteRepository.save(paciente);
    }

    @Override
    public Paciente actualizarPaciente(Long id, Paciente datos) {
        Paciente existente = pacienteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Paciente no encontrado con id " + id));

        // Si cambia documento, verificar no duplicado con otro paciente
        if (!existente.getNumeroDocumento().equals(datos.getNumeroDocumento())
                && existeDocumento(datos.getNumeroDocumento())) {
            throw new IllegalArgumentException("El nuevo documento ya está registrado por otro paciente.");
        }

        // Actualizar campos (RF-PAC-04 + edición)
        existente.setTipoDocumento(datos.getTipoDocumento());
        existente.setNumeroDocumento(datos.getNumeroDocumento());
        existente.setNombres(datos.getNombres());
        existente.setApellidoPaterno(datos.getApellidoPaterno());
        existente.setApellidoMaterno(datos.getApellidoMaterno());
        existente.setFechaNacimiento(datos.getFechaNacimiento());
        existente.setSexo(datos.getSexo());
        existente.setEstadoCivil(datos.getEstadoCivil());
        existente.setTelefono(datos.getTelefono());
        existente.setCorreo(datos.getCorreo());
        existente.setDireccion(datos.getDireccion());
        existente.setDistrito(datos.getDistrito());
        existente.setProvincia(datos.getProvincia());
        existente.setDepartamento(datos.getDepartamento());
        existente.setOcupacion(datos.getOcupacion());
        existente.setTipoSangre(datos.getTipoSangre());
        existente.setEstadoRegistro(datos.getEstadoRegistro());
        if (datos.getFotoUrl() != null) {
            existente.setFotoUrl(datos.getFotoUrl());
        }

        return pacienteRepository.save(existente);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Paciente> buscarPorId(Long id) {
        return pacienteRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Paciente> buscarPorCodigo(String codigo) {
        return pacienteRepository.findByCodigoPaciente(codigo);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Paciente> buscarPorDocumento(String numeroDocumento) {
        return pacienteRepository.findByNumeroDocumento(numeroDocumento);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeDocumento(String numeroDocumento) {
        return pacienteRepository.existsByNumeroDocumento(numeroDocumento);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Paciente> listarTodos() {
        return pacienteRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Paciente> buscar(String query) {
        if (query == null || query.isBlank()) {
            return listarTodos();
        }
        return pacienteRepository.buscarGeneral(query.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Paciente> busquedaAvanzada(String codigo, String documento, String nombres, String apellidos, String telefono) {
        boolean todosVacios = (codigo == null || codigo.isBlank())
                && (documento == null || documento.isBlank())
                && (nombres == null || nombres.isBlank())
                && (apellidos == null || apellidos.isBlank())
                && (telefono == null || telefono.isBlank());
        if (todosVacios) return listarTodos();

        return pacienteRepository.buscarAvanzada(
                blankToNull(codigo),
                blankToNull(documento),
                blankToNull(nombres),
                blankToNull(apellidos),
                blankToNull(telefono)
        );
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    @Override
    public void cambiarEstado(Long id, EstadoRegistro nuevoEstado) {
        Paciente p = pacienteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Paciente no encontrado"));
        // No eliminar físicamente: solo cambio de estado (incluye Fallecido)
        p.setEstadoRegistro(nuevoEstado);
        pacienteRepository.save(p);
    }

    @Override
    public void eliminarLogico(Long id) {
        cambiarEstado(id, EstadoRegistro.INACTIVO);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarPacientes() {
        return pacienteRepository.count();
    }

    // ===== Contactos =====
    @Override
    public ContactoEmergencia agregarContacto(Long pacienteId, ContactoEmergencia contacto) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new EntityNotFoundException("Paciente no encontrado"));
        // Si es principal, desmarcar otros
        if (Boolean.TRUE.equals(contacto.getEsPrincipal())) {
            contactoRepository.findByPacienteId(pacienteId).forEach(c -> {
                if (Boolean.TRUE.equals(c.getEsPrincipal())) {
                    c.setEsPrincipal(false);
                    contactoRepository.save(c);
                }
            });
        }
        contacto.setPaciente(paciente);
        return contactoRepository.save(contacto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactoEmergencia> listarContactos(Long pacienteId) {
        return contactoRepository.findByPacienteId(pacienteId);
    }

    @Override
    public void eliminarContacto(Long pacienteId, Long contactoId) {
        ContactoEmergencia c = contactoRepository.findById(contactoId)
                .orElseThrow(() -> new EntityNotFoundException("Contacto no encontrado"));
        if (!c.getPaciente().getId().equals(pacienteId)) {
            throw new IllegalArgumentException("El contacto no pertenece al paciente indicado");
        }
        contactoRepository.delete(c);
    }

    // ===== Seguros =====
    @Override
    public SeguroPaciente agregarSeguro(Long pacienteId, SeguroPaciente seguro) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new EntityNotFoundException("Paciente no encontrado"));
        seguro.setPaciente(paciente);
        return seguroRepository.save(seguro);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeguroPaciente> listarSeguros(Long pacienteId) {
        return seguroRepository.findByPacienteId(pacienteId);
    }

    @Override
    public void eliminarSeguro(Long pacienteId, Long seguroId) {
        SeguroPaciente s = seguroRepository.findById(seguroId)
                .orElseThrow(() -> new EntityNotFoundException("Seguro no encontrado"));
        if (!s.getPaciente().getId().equals(pacienteId)) throw new IllegalArgumentException("Seguro no pertenece al paciente");
        seguroRepository.delete(s);
    }

    // ===== Antecedentes =====
    @Override
    public Antecedente agregarAntecedente(Long pacienteId, Antecedente antecedente) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new EntityNotFoundException("Paciente no encontrado"));
        antecedente.setPaciente(paciente);
        return antecedenteRepository.save(antecedente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Antecedente> listarAntecedentes(Long pacienteId) {
        return antecedenteRepository.findByPacienteId(pacienteId);
    }

    @Override
    public void eliminarAntecedente(Long pacienteId, Long antecedenteId) {
        Antecedente a = antecedenteRepository.findById(antecedenteId)
                .orElseThrow(() -> new EntityNotFoundException("Antecedente no encontrado"));
        if (!a.getPaciente().getId().equals(pacienteId)) throw new IllegalArgumentException("Antecedente no pertenece al paciente");
        antecedenteRepository.delete(a);
    }
}
