package com.alex.paciente.entity;

import com.alex.paciente.entity.enums.CategoriaAntecedente;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "antecedente", indexes = {
        @Index(name = "idx_antecedente_paciente", columnList = "paciente_id"),
        @Index(name = "idx_antecedente_tipo", columnList = "tipo")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Antecedente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @NotNull(message = "La categoría es obligatoria")
    private CategoriaAntecedente categoria;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "El tipo es obligatorio")
    private String tipo; // Ej: Diabetes, Hipertensión, Medicamento, Cirugía, etc.

    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    @Column(length = 255)
    private String reaccion; // Solo para alergias: reacción presentada

    @Column(name = "fecha_diagnostico")
    private LocalDate fechaDiagnostico; // Req.5 feature - fecha diagnostico opcional

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true; // Req.5 feature - soft delete

    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
        if (this.activo == null) this.activo = true;
    }
}
