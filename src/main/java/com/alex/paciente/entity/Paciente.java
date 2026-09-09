package com.alex.paciente.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad central del sistema.
 * Relaciones:
 * - @OneToOne con HistoriaClinica
 * - @OneToMany con Antecedente (@ManyToOne del otro lado)
 * - @OneToMany con PacienteAlergia (detalle N-M con atributos)
 * - @ManyToMany con Alergia (vista de catálogo, solo lectura; escritura vía PacienteAlergia)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "paciente",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_paciente_codigo", columnNames = "codigo"),
                @UniqueConstraint(name = "uk_paciente_dni", columnNames = "dni")
        },
        indexes = {
                @Index(name = "idx_paciente_nombres", columnList = "nombres, apellido_paterno, apellido_materno"),
                @Index(name = "idx_paciente_telefono", columnList = "telefono")
        })
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String codigo;

    @Column(nullable = false, length = 15)
    private String dni;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellido_paterno", nullable = false, length = 80)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", length = 80)
    private String apellidoMaterno;

    @Column(length = 20)
    private String telefono;

    @Column(length = 120)
    private String email;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(length = 20)
    private String sexo;

    @Column(length = 200)
    private String direccion;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    void prePersist() {
        if (fechaRegistro == null) fechaRegistro = LocalDateTime.now();
        if (activo == null) activo = true;
    }

    // @OneToOne con HistoriaClinica
    @OneToOne(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private HistoriaClinica historiaClinica;

    // @OneToMany con Antecedente
    @Builder.Default
    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Antecedente> antecedentes = new HashSet<>();

    // @OneToMany con detalle de alergias (N-M con atributos: reacción, severidad)
    @Builder.Default
    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<PacienteAlergia> pacienteAlergias = new HashSet<>();

    // @ManyToMany con Alergia (vista solo lectura del mismo join table).
    // La escritura se hace vía PacienteAlergia para guardar reacción/severidad.
    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "paciente_alergia",
            joinColumns = @JoinColumn(name = "paciente_id", insertable = false, updatable = false),
            inverseJoinColumns = @JoinColumn(name = "alergia_id", insertable = false, updatable = false),
            uniqueConstraints = @UniqueConstraint(columnNames = {"paciente_id", "alergia_id"}))
    private Set<Alergia> alergias = new HashSet<>();
}
