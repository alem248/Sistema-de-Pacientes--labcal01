package com.alex.paciente.entity;

import com.alex.paciente.entity.enums.CategoriaAntecedente;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad Antecedente UNIFICADA.
 * Integra:
 * - Companero (MVC): categoria (PERSONAL/FAMILIAR/ALERGIA), tipo String libre,
 *   descripcion, reaccion (solo alergias).
 * - Req.05 (REST): tipoAntecedente enum, categoriaDetalle enum detallado,
 *   fechaDiagnostico, activo. El servicio REST sincroniza ambos modelos.
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
                @Index(name = "idx_antecedente_categoria", columnList = "categoria")
        })
public class Antecedente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_antecedente_paciente"))
    private Paciente paciente;

    // Modelo companero (usado por Thymeleaf MVC)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaAntecedente categoria;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "El tipo es obligatorio")
    private String tipo;

    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    @Column(length = 255)
    private String reaccion;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    // Req.05: detalle tipado (usado por API REST). Nulleable para no romper registros MVC.
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_antecedente", length = 20)
    private com.alex.paciente.enums.TipoAntecedente tipoAntecedente;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_detalle", length = 40)
    private com.alex.paciente.enums.CategoriaAntecedente categoriaDetalle;

    @Column(name = "fecha_diagnostico")
    private LocalDate fechaDiagnostico;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
        if (this.activo == null) this.activo = true;
    }
}
