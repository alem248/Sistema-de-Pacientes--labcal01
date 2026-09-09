package com.alex.paciente.service;

import com.alex.paciente.entity.Antecedente;
import com.alex.paciente.entity.ContactoEmergencia;
import com.alex.paciente.entity.Paciente;
import com.alex.paciente.entity.SeguroPaciente;
import com.alex.paciente.entity.enums.EstadoRegistro;

import java.util.List;
import java.util.Optional;

public interface PacienteService {

    // RF-PAC-03: Generar código único
    String generarCodigoPaciente();

    // RF-PAC-04: Registrar información personal y contacto + validaciones
    Paciente registrarPaciente(Paciente paciente);

    Paciente actualizarPaciente(Long id, Paciente pacienteActualizado);

    Optional<Paciente> buscarPorId(Long id);

    Optional<Paciente> buscarPorCodigo(String codigo);

    Optional<Paciente> buscarPorDocumento(String numeroDocumento);

    boolean existeDocumento(String numeroDocumento);

    List<Paciente> listarTodos();

    List<Paciente> buscar(String query);

    List<Paciente> busquedaAvanzada(String codigo, String documento, String nombres, String apellidos, String telefono);

    void cambiarEstado(Long id, EstadoRegistro nuevoEstado);

    void eliminarLogico(Long id); // Inactivar, no borrar físico

    long contarPacientes();

    // Contactos
    ContactoEmergencia agregarContacto(Long pacienteId, ContactoEmergencia contacto);
    List<ContactoEmergencia> listarContactos(Long pacienteId);
    void eliminarContacto(Long pacienteId, Long contactoId);

    // Seguros
    SeguroPaciente agregarSeguro(Long pacienteId, SeguroPaciente seguro);
    List<SeguroPaciente> listarSeguros(Long pacienteId);
    void eliminarSeguro(Long pacienteId, Long seguroId);

    // Antecedentes
    Antecedente agregarAntecedente(Long pacienteId, Antecedente antecedente);
    List<Antecedente> listarAntecedentes(Long pacienteId);
    void eliminarAntecedente(Long pacienteId, Long antecedenteId);
}
