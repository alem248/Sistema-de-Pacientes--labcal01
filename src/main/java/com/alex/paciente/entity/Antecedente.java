package com.alex.paciente.entity;

import com.alex.paciente.enums.CategoriaAntecedente;
import com.alex.paciente.enums.TipoAntecedente;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Req.5 - Antecedentes del paciente.
 * Relación @ManyToOne con Paciente (muchos antecedentes -> un paciente).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "antecedente",
        indexes = {
                @Index(name = "idx_antecedente_paciente", columnList = "paciente_id"),
                @Index(name = "idx_antecedente_tipo", columnList = "tipo")
        })
public class Antecedente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoAntecedente tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private CategoriaAntecedente categoria;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_diagnostico")
    private LocalDate fechaDiagnostico;

    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_antecedente_paciente"))
    private Paciente paciente;

    @PrePersist
    void prePersist() {
        if (fechaRegistro == null) fechaRegistro = LocalDateTime.now();
        if (activo == null) activo = true;
    }
}
