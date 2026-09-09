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

/**
 * Entidad Paciente UNIFICADA.
 * Integra:
 * - RF-PAC-03/04 companero: codigoPaciente, tipoDocumento/numeroDocumento, datos personales,
 *   ubicacion, contacto, estadoRegistro, foto, contactos, seguros, antecedentes List.
 * - Req.05-06 (REST): historiaClinica @OneToOne, pacienteAlergias + alergias @ManyToMany,
 *   indices de busqueda, aliases codigo/dni/email/activo para compatibilidad API.
 */
@Entity
@Table(name = "paciente",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_paciente_codigo", columnNames = "codigo_paciente"),
                @UniqueConstraint(name = "uk_paciente_documento", columnNames = "numero_documento")
        },
        indexes = {
                @Index(name = "idx_paciente_nombres", columnList = "nombres, apellido_paterno, apellido_materno"),
                @Index(name = "idx_paciente_telefono", columnList = "telefono")
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
    @Column(name = "tipo_documento", length = 20)
    @Builder.Default
    private TipoDocumento tipoDocumento = TipoDocumento.DNI;

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

    @Column(name = "apellido_materno", length = 100)
    @Builder.Default
    private String apellidoMaterno = "-";

    @Column(name = "fecha_nacimiento")
    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private Sexo sexo = Sexo.OTRO;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_civil", length = 30)
    @Builder.Default
    private EstadoCivil estadoCivil = EstadoCivil.SOLTERO;

    // Ubicación y contacto - RF-PAC-04 (nullable para compatibilidad REST Req.06)
    @Column(length = 20)
    @Pattern(regexp = "^[0-9+\\-\\s]{7,20}$", message = "Teléfono inválido")
    private String telefono;

    @Column(length = 150)
    @Email(message = "Correo electrónico inválido")
    private String correo;

    @Column(length = 255)
    @Builder.Default
    private String direccion = "S/N";

    @Column(length = 100)
    @Builder.Default
    private String distrito = "Lima";

    @Column(length = 100)
    @Builder.Default
    private String provincia = "Lima";

    @Column(length = 100)
    @Builder.Default
    private String departamento = "Lima";

    @Column(length = 100)
    private String ocupacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_sangre", length = 20)
    private TipoSangre tipoSangre;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_registro", nullable = false, length = 20)
    @Builder.Default
    private EstadoRegistro estadoRegistro = EstadoRegistro.ACTIVO;

    // Fotografía del paciente (opcional, si el hospital lo requiere)
    @Column(name = "foto_url", length = 255)
    private String fotoUrl;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Relaciones companero (MVC)
    @JsonIgnore
    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ContactoEmergencia> contactos = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SeguroPaciente> seguros = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Antecedente> antecedentes = new ArrayList<>();

    // Req.05: @OneToOne con HistoriaClinica
    @JsonIgnore
    @OneToOne(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private HistoriaClinica historiaClinica;

    // Req.05: detalle N-M alergias con atributos (reacción, severidad)
    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<PacienteAlergia> pacienteAlergias = new HashSet<>();

    // Req.05: @ManyToMany vista solo lectura (escritura vía PacienteAlergia)
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
        if (this.tipoDocumento == null) this.tipoDocumento = TipoDocumento.DNI;
        if (this.sexo == null) this.sexo = Sexo.OTRO;
        if (this.estadoCivil == null) this.estadoCivil = EstadoCivil.SOLTERO;
        if (this.apellidoMaterno == null) this.apellidoMaterno = "-";
        if (this.direccion == null) this.direccion = "S/N";
        if (this.distrito == null) this.distrito = "Lima";
        if (this.provincia == null) this.provincia = "Lima";
        if (this.departamento == null) this.departamento = "Lima";
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
        return String.format("%s %s %s", nombres, apellidoPaterno,
                apellidoMaterno == null ? "" : apellidoMaterno).trim();
    }

    // Helpers para relaciones (companero)
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

    // ---------- Aliases @Transient para compatibilidad API REST Req.06 ----------
    // La API REST usa codigo/dni/email/activo; el modelo canonico usa
    // codigoPaciente/numeroDocumento/correo/estadoRegistro.
    @Transient
    public String getCodigo() {
        return this.codigoPaciente;
    }

    @Transient
    public void setCodigo(String codigo) {
        this.codigoPaciente = codigo;
    }

    @Transient
    public String getDni() {
        return this.numeroDocumento;
    }

    @Transient
    public void setDni(String dni) {
        this.numeroDocumento = dni;
    }

    @Transient
    public String getEmail() {
        return this.correo;
    }

    @Transient
    public void setEmail(String email) {
        this.correo = email;
    }

    @Transient
    public Boolean getActivo() {
        return this.estadoRegistro == EstadoRegistro.ACTIVO;
    }

    @Transient
    public void setActivo(Boolean activo) {
        if (activo == null) return;
        this.estadoRegistro = activo ? EstadoRegistro.ACTIVO : EstadoRegistro.INACTIVO;
    }
}
