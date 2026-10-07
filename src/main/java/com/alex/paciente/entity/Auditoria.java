package com.alex.paciente.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Bitácora de auditoría (Pregunta 2 - Evaluación 02).
 * Registra automáticamente: usuario, fecha/hora, operación, entidad afectada
 * e identificador del registro afectado.
 */
@Entity
@Table(name = "auditoria",
        indexes = {
                @Index(name = "idx_auditoria_entidad", columnList = "entidad, entidad_id"),
                @Index(name = "idx_auditoria_fecha", columnList = "fecha_hora"),
                @Index(name = "idx_auditoria_usuario", columnList = "usuario")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Usuario autenticado que realizó la operación (o "sistema" si no hay sesión). */
    @Column(nullable = false, length = 50)
    private String usuario;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora;

    /** Operación realizada: REGISTRO, MODIFICACION, ELIMINACION, CONSULTA. */
    @Column(nullable = false, length = 20)
    private String operacion;

    /** Entidad afectada: Paciente, Usuario, Rol, HistoriaClinica, etc. */
    @Column(nullable = false, length = 50)
    private String entidad;

    /** Identificador del registro afectado. */
    @Column(name = "entidad_id", length = 50)
    private String entidadId;

    /** Detalle legible de la operación (valores relevantes, sin datos sensibles). */
    @Column(length = 500)
    private String detalle;

    @PrePersist
    protected void onCreate() {
        if (this.fechaHora == null) this.fechaHora = LocalDateTime.now();
    }
}
