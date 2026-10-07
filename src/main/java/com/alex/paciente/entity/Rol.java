package com.alex.paciente.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Rol del sistema para control de acceso (Pregunta 3 - Evaluación 02).
 * Roles mínimos: ADMINISTRADOR, MEDICO, RECEPCIONISTA.
 * Relación: un Rol tiene muchos Usuarios (@OneToMany lado inverso).
 */
@Entity
@Table(name = "rol",
        uniqueConstraints = @UniqueConstraint(name = "uk_rol_nombre", columnNames = "nombre"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    @NotBlank(message = "El nombre del rol es obligatorio")
    @Size(max = 30)
    private String nombre; // ADMINISTRADOR, MEDICO, RECEPCIONISTA

    @Column(length = 200)
    private String descripcion;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.activo == null) this.activo = true;
    }
}
