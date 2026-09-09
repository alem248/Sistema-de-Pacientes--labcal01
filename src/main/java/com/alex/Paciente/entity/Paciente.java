package com.alex.Paciente.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

/**
 * RF-PAC-01: Registro de pacientes
 * RF-PAC-02: Validacion del numero de documento
 */
@Entity
@Table(name = "pacientes", uniqueConstraints = {
        @UniqueConstraint(columnNames = "numero_documento", name = "uk_numero_documento")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El tipo de documento es obligatorio")
    @Column(name = "tipo_documento", nullable = false, length = 20)
    private String tipoDocumento; // DNI, CE, Pasaporte

    @NotBlank(message = "El numero de documento es obligatorio")
    @Column(name = "numero_documento", nullable = false, unique = true, length = 20)
    private String numeroDocumento;

    @NotBlank(message = "Los nombres son obligatorios")
    @Column(nullable = false, length = 100)
    private String nombres;

    @NotBlank(message = "El apellido paterno es obligatorio")
    @Column(name = "apellido_paterno", nullable = false, length = 100)
    private String apellidoPaterno;

    @NotBlank(message = "El apellido materno es obligatorio")
    @Column(name = "apellido_materno", nullable = false, length = 100)
    private String apellidoMaterno;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    // Edad calculada automaticamente - no se persiste (RF-PAC-01)
    @Transient
    private Integer edad;

    @NotBlank(message = "El sexo es obligatorio")
    @Column(nullable = false, length = 20)
    private String sexo; // Masculino, Femenino, Otro

    @Column(name = "estado_civil", length = 30)
    private String estadoCivil; // Soltero, Casado, Divorciado, Viudo, etc.

    @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "Formato de telefono no valido")
    @Column(length = 20)
    private String telefono;

    @Email(message = "El correo electronico no es valido")
    @Column(name = "correo_electronico", length = 100)
    private String correoElectronico;

    @Column(length = 255)
    private String direccion;

    @Column(length = 100)
    private String distrito;

    @Column(length = 100)
    private String provincia;

    @Column(length = 100)
    private String departamento;

    @Column(length = 100)
    private String ocupacion;

    @Column(name = "tipo_sangre", length = 10)
    private String tipoSangre; // A+, A-, B+, B-, AB+, AB-, O+, O-

    @NotBlank(message = "El estado es obligatorio")
    @Builder.Default
    @Column(nullable = false, length = 20)
    private String estado = "Activo"; // Activo, Inactivo, Fallecido

    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
    }

    /**
     * Calcula la edad automaticamente a partir de la fecha de nacimiento
     * RF-PAC-01: Edad calculada automaticamente
     */
    public Integer getEdad() {
        if (fechaNacimiento == null) {
            return null;
        }
        return Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }

    // Para Thymeleaf: permite acceder a edad calculada
    public void setEdad(Integer edad) {
        this.edad = edad;
    }

    // Metodo auxiliar para mostrar nombre completo
    public String getNombreCompleto() {
        return String.format("%s %s %s", nombres, apellidoPaterno, apellidoMaterno);
    }
}
