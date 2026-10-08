package com.alex.paciente.dto;

import com.alex.paciente.entity.Paciente;
import com.alex.paciente.repository.PacienteRepository;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * DTO para crear un nuevo paciente.
 * Utiliza anotaciones de validación de Jakarta para validar datos personales y de contacto.
 * Este DTO es el objeto de entrada al controlador REST @PostMapping.
 */
public record PacienteCreateDTO(

        /** Nombre(s) del paciente. No puede estar vacío. */
        @NotBlank(message = "Los nombres son obligatorios")
        @Size(max = 100, message = "Los nombres deben tener como máximo 100 caracteres")
        String nombres,

        /** Apellido paterno del paciente. No puede estar vacío. */
        @NotBlank(message = "El apellido paterno es obligatorio")
        @Size(max = 100, message = "El apellido paterno debe tener como máximo 100 caracteres")
        String apellidoPaterno,

        /** Apellido materno del paciente. */
        @Size(max = 100, message = "El apellido materno debe tener como máximo 100 caracteres")
        String apellidoMaterno,

        /** Tipo de documento de identidad. */
        @NotBlank(message = "El tipo de documento es obligatorio")
        @Size(max = 20, message = "El tipo de documento debe tener como máximo 20 caracteres")
        String tipoDocumento,

        /** Número de documento de identidad. Debe tener entre 8 y 20 caracteres. */
        @NotBlank(message = "El número de documento es obligatorio")
        @Size(min = 8, max = 20, message = "El número de documento debe tener entre 8 y 20 caracteres")
        String numeroDocumento,

        /** Fecha de nacimiento. Debe ser una fecha en el pasado. */
        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento debe ser una fecha en el pasado")
        LocalDate fechaNacimiento,

        /** Género del paciente. */
        @NotBlank(message = "El género es obligatorio")
        @Size(max = 20, message = "El género debe tener como máximo 20 caracteres")
        String sexo,

        /** Correo electrónico del paciente. Debe ser un email válido. */
        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El correo electrónico debe tener un formato válido")
        @Size(max = 150, message = "El correo electrónico debe tener como máximo 150 caracteres")
        String email,

        /** Teléfono del paciente. Formato: solo números, +, -, espacios. */
        @Pattern(regexp = "^[0-9+\\-\\s]{7,20}$", message = "El teléfono debe tener entre 7 y 20 dígitos")
        @Size(min = 7, max = 20, message = "El teléfono debe tener entre 7 y 20 caracteres")
        String telefono,

        /** Dirección del paciente. */
        @Size(max = 255, message = "La dirección debe tener como máximo 255 caracteres")
        String direccion

) {
    /**
     * Convierte este DTO a entidad Paciente, generando automáticamente el código único.
     *
     * @param pacienteRepository Repositorio para verificar existencia de códigos y documentos
     * @return Entidad Paciente completa y persistente
     */
    public Paciente toEntity(PacienteRepository pacienteRepository) {
        // Generar código único con formato PAC-YYYYMMDD-XXXX
        String codigoUnico = generarCodigoUnico(pacienteRepository);

        return Paciente.builder()
                .codigoPaciente(codigoUnico)
                .tipoDocumento(convertirTipoDocumento(tipoDocumento))
                .numeroDocumento(numeroDocumento)
                .nombres(nombres)
                .apellidoPaterno(apellidoPaterno)
                .apellidoMaterno(apellidoMaterno)
                .fechaNacimiento(fechaNacimiento)
                .sexo(convertirSexo(sexo))
                .correo(email)
                .telefono(telefono)
                .direccion(direccion)
                .build();
    }

    private static com.alex.paciente.entity.enums.TipoDocumento convertirTipoDocumento(String valor) {
        if (valor == null || valor.isBlank()) {
            return com.alex.paciente.entity.enums.TipoDocumento.DNI;
        }
        try {
            return com.alex.paciente.entity.enums.TipoDocumento.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return com.alex.paciente.entity.enums.TipoDocumento.OTRO;
        }
    }

    private static com.alex.paciente.entity.enums.Sexo convertirSexo(String valor) {
        if (valor == null || valor.isBlank()) {
            return com.alex.paciente.entity.enums.Sexo.OTRO;
        }
        String v = valor.trim().toUpperCase();
        if (v.startsWith("M")) return com.alex.paciente.entity.enums.Sexo.MASCULINO;
        if (v.startsWith("F")) return com.alex.paciente.entity.enums.Sexo.FEMENINO;
        return com.alex.paciente.entity.enums.Sexo.OTRO;
    }

    /**
     * Genera un código único con formato PAC-YYYYMMDD-XXXX.
     * Ejemplos: PAC-20240115-0001, PAC-20260928-0042
     *
     * @param pacienteRepository Repositorio para verificar colisiones
     * @return Código único generado
     */
    private static String generarCodigoUnico(PacienteRepository pacienteRepository) {
        // Obtener la fecha actual formateada
        String fechaFormateada = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));

        // Intentar encontrar un código disponible empezando por 0001
        for (int i = 1; i <= 9999; i++) {
            String codigo = String.format("PAC-%s-%04d", fechaFormateada, i);
            if (!pacienteRepository.existsByCodigoPaciente(codigo)) {
                return codigo;
            }
        }

        // Si todos los códigos del día están ocupados, usar UUID como fallback
        return "PAC-" + LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + java.util.UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}