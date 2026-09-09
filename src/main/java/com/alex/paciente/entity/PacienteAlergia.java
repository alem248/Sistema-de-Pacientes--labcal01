package com.alex.paciente.entity;

import com.alex.paciente.enums.SeveridadAlergia;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Tabla puente Paciente <-> Alergia con atributos propios.
 * Implementa el @ManyToMany con payload (reacción presentada, severidad).
 * Relaciones @ManyToOne hacia ambos lados.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "paciente_alergia",
        uniqueConstraints = @UniqueConstraint(name = "uk_paciente_alergia", columnNames = {"paciente_id", "alergia_id"}),
        indexes = @Index(name = "idx_pacalergia_paciente", columnList = "paciente_id"))
public class PacienteAlergia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_pacalergia_paciente"))
    private Paciente paciente;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alergia_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_pacalergia_alergia"))
    private Alergia alergia;

    /** Req.5 - Reacción presentada (ej. urticaria, anafilaxia, vómitos). */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String reaccion;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SeveridadAlergia severidad;

    @Column(name = "fecha_registro")
    private LocalDate fechaRegistro;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @PrePersist
    void prePersist() {
        if (fechaRegistro == null) fechaRegistro = LocalDate.now();
    }
}
