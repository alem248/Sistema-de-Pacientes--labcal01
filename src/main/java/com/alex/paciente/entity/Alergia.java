package com.alex.paciente.entity;

import com.alex.paciente.enums.TipoAlergia;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Req.5 - Catálogo de alergias (medicamentos, alimentos, otras).
 * Relación @ManyToMany con Paciente + @OneToMany con PacienteAlergia (detalle con reacción).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "alergia",
        uniqueConstraints = @UniqueConstraint(name = "uk_alergia_nombre", columnNames = "nombre"))
public class Alergia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoAlergia tipo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @JsonIgnore
    @Builder.Default
    @ManyToMany(mappedBy = "alergias", fetch = FetchType.LAZY)
    private Set<Paciente> pacientes = new HashSet<>();

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "alergia", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<PacienteAlergia> pacienteAlergias = new HashSet<>();
}
