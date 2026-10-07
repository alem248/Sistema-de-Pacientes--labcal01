package com.alex.paciente.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * RF-PAC-06: información COMPLETA del paciente en una sola respuesta.
 * Incluye datos personales, ubicación, contactos de emergencia, seguros,
 * antecedentes, alergias e historia clínica.
 */
public record PacienteDetalleResponseDTO(
        // Identificación y datos personales
        Long id,
        String codigo,
        String tipoDocumento,
        String numeroDocumento,
        String nombres,
        String apellidoPaterno,
        String apellidoMaterno,
        String nombreCompleto,
        LocalDate fechaNacimiento,
        Integer edad,
        String sexo,
        String estadoCivil,
        String tipoSangre,
        String ocupacion,
        String estadoRegistro,
        // Contacto y ubicación
        String telefono,
        String correo,
        String direccion,
        String distrito,
        String provincia,
        String departamento,
        String fotoUrl,
        // Auditoría del registro
        LocalDateTime fechaRegistro,
        LocalDateTime fechaActualizacion,
        // Historia clínica (@OneToOne)
        String numeroHistoria,
        LocalDate fechaAperturaHistoria,
        String observacionesHistoria,
        // Relaciones (@OneToMany / @ManyToMany)
        List<ContactoDTO> contactos,
        List<SeguroDTO> seguros,
        List<AntecedenteDTO> antecedentes,
        List<AlergiaDTO> alergias
) {
    public record ContactoDTO(Long id, String nombreCompleto, String parentesco,
                              String telefono, String direccion, String correo, Boolean esPrincipal) {}

    public record SeguroDTO(Long id, String tipoSeguro, String empresaAseguradora,
                            String numeroPoliza, String numeroAfiliacion,
                            LocalDate fechaInicio, LocalDate fechaVencimiento, String estadoCobertura) {}

    public record AntecedenteDTO(Long id, String categoria, String tipo, String descripcion,
                                 String reaccion, LocalDateTime fechaRegistro) {}

    public record AlergiaDTO(Long id, String nombre, String tipo, String reaccion,
                             String severidad, LocalDate fechaRegistro) {}
}
