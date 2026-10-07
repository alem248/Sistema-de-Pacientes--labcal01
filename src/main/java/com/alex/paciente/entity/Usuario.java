package com.alex.paciente.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Usuario del sistema (Pregunta 3 - Evaluación 02).
 * Relación @ManyToOne con Rol: cada usuario tiene exactamente un rol asignado.
 * La contraseña se almacena con hash BCrypt (nunca en texto plano).
 */
@Entity
@Table(name = "usuario",
        uniqueConstraints = @UniqueConstraint(name = "uk_usuario_username", columnNames = "username"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 4, max = 50, message = "El usuario debe tener entre 4 y 50 caracteres")
    private String username;

    /** Hash BCrypt de la contraseña. Nunca se expone en JSON. */
    @JsonIgnore
    @Column(nullable = false, length = 100)
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    @Column(name = "nombre_completo", nullable = false, length = 150)
    @NotBlank(message = "El nombre completo es obligatorio")
    private String nombreCompleto;

    @Column(length = 150)
    @Email(message = "Correo electrónico inválido")
    private String correo;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    /** Relación Usuario -> Rol (muchos usuarios pueden compartir un rol). */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "rol_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_usuario_rol"))
    private Rol rol;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.activo == null) this.activo = true;
    }
}
