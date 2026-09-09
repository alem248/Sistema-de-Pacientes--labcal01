package com.alex.paciente.entity;

import com.alex.paciente.entity.enums.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "paciente", uniqueConstraints = {
        @UniqueConstraint(columnNames = "codigo_paciente"),
        @UniqueConstraint(columnNames = "numero_documento")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // RF-PAC-03: Código único generado automáticamente (PAC-000001)
    @Column(name = "codigo_paciente", nullable = false, unique = true, length = 20)
    private String codigoPaciente;

    // Identificación del paciente
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 20)
    @NotNull(message = "El tipo de documento es obligatorio")
    private TipoDocumento tipoDocumento;

    @Column(name = "numero_documento", nullable = false, unique = true, length = 20)
    @NotBlank(message = "El número de documento es obligatorio")
    @Size(min = 8, max = 20, message = "El número de documento debe tener entre 8 y 20 caracteres")
    private String numeroDocumento;

    // Datos personales - RF-PAC-04
    @Column(nullable = false, length = 100)
    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 100)
    private String nombres;

    @Column(name = "apellido_paterno", nullable = false, length = 100)
    @NotBlank(message = "El apellido paterno es obligatorio")
    private String apellidoPaterno;

    @Column(name = "apellido_materno", nullable = false, length = 100)
    @NotBlank(message = "El apellido materno es obligatorio")
    private String apellidoMaterno;

    @Column(name = "fecha_nacimiento", nullable = false)
    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull(message = "El sexo es obligatorio")
    private Sexo sexo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_civil", nullable = false, length = 30)
    @NotNull(message = "El estado civil es obligatorio")
    private EstadoCivil estadoCivil;

    // Ubicación y contacto - RF-PAC-04
    @Column(nullable = false, length = 20)
    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9+\\-\\s]{7,20}$", message = "Teléfono inválido")
    private String telefono;

    @Column(length = 150)
    @Email(message = "Correo electrónico inválido")
    private String correo;

    @Column(nullable = false, length = 255)
    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "El distrito es obligatorio")
    private String distrito;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "La provincia es obligatoria")
    private String provincia;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "El departamento es obligatorio")
    private String departamento;

    @Column(length = 100)
    private String ocupacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_sangre", length = 20)
    private TipoSangre tipoSangre;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_registro", nullable = false, length = 20)
    @NotNull(message = "El estado es obligatorio")
    @Builder.Default
    private EstadoRegistro estadoRegistro = EstadoRegistro.ACTIVO;

    // Fotografía del paciente (opcional, si el hospital lo requiere)
    @Column(name = "foto_url", length = 255)
    private String fotoUrl;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Relaciones
    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ContactoEmergencia> contactos = new ArrayList<>();

    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SeguroPaciente> seguros = new ArrayList<>();

    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Antecedente> antecedentes = new ArrayList<>();

    // Vinculación Req.5/6 feature: Historia clínica y alergias (compatibilidad con rama feature/req-05-06)
    @JsonIgnore
    @OneToOne(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private HistoriaClinica historiaClinica;

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<PacienteAlergia> pacienteAlergias = new HashSet<>();

    @JsonIgnore
    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "paciente_alergia",
            joinColumns = @JoinColumn(name = "paciente_id", insertable = false, updatable = false),
            inverseJoinColumns = @JoinColumn(name = "alergia_id", insertable = false, updatable = false))
    private Set<Alergia> alergias = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        if (this.estadoRegistro == null) {
            this.estadoRegistro = EstadoRegistro.ACTIVO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    // Edad calculada automáticamente (no se almacena)
    @Transient
    public int getEdad() {
        if (fechaNacimiento == null) return 0;
        return Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }

    @Transient
    public String getNombreCompleto() {
        return String.format("%s %s %s", nombres, apellidoPaterno, apellidoMaterno).trim();
    }

    // Aliases para compatibilidad con feature Req.5/6 (codigo/dni/email/activo)
    @Transient
    public String getCodigo() { return codigoPaciente; }
    @Transient
    public String getDni() { return numeroDocumento; }
    @Transient
    public String getEmail() { return correo; }
    @Transient
    public Boolean getActivo() { return estadoRegistro == EstadoRegistro.ACTIVO; }

    // Helpers para relaciones
    public void addContacto(ContactoEmergencia contacto) {
        contactos.add(contacto);
        contacto.setPaciente(this);
    }

    public void removeContacto(ContactoEmergencia contacto) {
        contactos.remove(contacto);
        contacto.setPaciente(null);
    }

    public void addSeguro(SeguroPaciente seguro) {
        seguros.add(seguro);
        seguro.setPaciente(this);
    }

    public void addAntecedente(Antecedente antecedente) {
        antecedentes.add(antecedente);
        antecedente.setPaciente(this);
    }

    public void addPacienteAlergia(PacienteAlergia pa) {
        pacienteAlergias.add(pa);
        pa.setPaciente(this);
    }
}
