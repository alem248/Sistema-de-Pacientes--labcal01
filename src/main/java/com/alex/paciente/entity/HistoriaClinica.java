package com.alex.paciente.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Historia clínica del paciente.
 * Relación @OneToOne con Paciente (lado dueño).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "historia_clinica",
        uniqueConstraints = @UniqueConstraint(name = "uk_historia_numero", columnNames = "numero_historia"))
public class HistoriaClinica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_historia", nullable = false, length = 30)
    private String numeroHistoria;

    @Column(name = "fecha_apertura")
    private LocalDate fechaApertura;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_historia_paciente"))
    private Paciente paciente;

    @PrePersist
    void prePersist() {
        if (fechaApertura == null) fechaApertura = LocalDate.now();
    }
}
