package com.alex.paciente.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "contacto_emergencia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactoEmergencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @Column(name = "nombre_completo", nullable = false, length = 150)
    @NotBlank(message = "El nombre completo del contacto es obligatorio")
    private String nombreCompleto;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "El parentesco es obligatorio")
    private String parentesco;

    @Column(nullable = false, length = 20)
    @NotBlank(message = "El teléfono del contacto es obligatorio")
    private String telefono;

    @Column(length = 255)
    private String direccion;

    @Column(length = 150)
    @Email(message = "Correo del contacto inválido")
    private String correo;

    @Column(name = "es_principal", nullable = false)
    @Builder.Default
    private Boolean esPrincipal = false;
}
