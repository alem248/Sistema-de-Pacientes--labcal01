package com.alex.paciente.entity;

import com.alex.paciente.entity.enums.EstadoCobertura;
import com.alex.paciente.entity.enums.TipoSeguro;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity
@Table(name = "seguro_paciente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeguroPaciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_seguro", nullable = false, length = 30)
    @NotNull(message = "El tipo de seguro es obligatorio")
    private TipoSeguro tipoSeguro;

    @Column(name = "empresa_aseguradora", nullable = false, length = 150)
    @NotBlank(message = "La empresa aseguradora es obligatoria")
    private String empresaAseguradora;

    @Column(name = "numero_poliza", length = 50)
    private String numeroPoliza;

    @Column(name = "numero_afiliacion", length = 50)
    private String numeroAfiliacion;

    @Column(name = "fecha_inicio")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInicio;

    @Column(name = "fecha_vencimiento")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_cobertura", nullable = false, length = 20)
    @NotNull
    @Builder.Default
    private EstadoCobertura estadoCobertura = EstadoCobertura.ACTIVO;
}
