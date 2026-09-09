package com.alex.paciente.repository;

import com.alex.paciente.entity.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    Optional<Paciente> findByCodigoPaciente(String codigoPaciente);

    Optional<Paciente> findByNumeroDocumento(String numeroDocumento);

    boolean existsByNumeroDocumento(String numeroDocumento);

    boolean existsByCodigoPaciente(String codigoPaciente);

    Optional<Paciente> findTopByOrderByIdDesc();

    // Búsqueda general: DNI, código, nombres, apellidos, teléfono, historia clínica (código)
    // Historia clínica se mapea al codigoPaciente en este sistema
    @Query("""
        SELECT p FROM Paciente p WHERE
        LOWER(p.numeroDocumento) LIKE LOWER(CONCAT('%', :q, '%')) OR
        LOWER(p.codigoPaciente) LIKE LOWER(CONCAT('%', :q, '%')) OR
        LOWER(p.nombres) LIKE LOWER(CONCAT('%', :q, '%')) OR
        LOWER(p.apellidoPaterno) LIKE LOWER(CONCAT('%', :q, '%')) OR
        LOWER(p.apellidoMaterno) LIKE LOWER(CONCAT('%', :q, '%')) OR
        LOWER(p.telefono) LIKE LOWER(CONCAT('%', :q, '%')) OR
        LOWER(CONCAT(p.nombres, ' ', p.apellidoPaterno, ' ', p.apellidoMaterno)) LIKE LOWER(CONCAT('%', :q, '%'))
        """)
    List<Paciente> buscarGeneral(@Param("q") String q);

    @Query("SELECT p FROM Paciente p WHERE " +
            "(:codigo IS NULL OR p.codigoPaciente LIKE %:codigo%) AND " +
            "(:documento IS NULL OR p.numeroDocumento LIKE %:documento%) AND " +
            "(:nombres IS NULL OR LOWER(p.nombres) LIKE LOWER(CONCAT('%', :nombres, '%'))) AND " +
            "(:apellidos IS NULL OR LOWER(p.apellidoPaterno) LIKE LOWER(CONCAT('%', :apellidos, '%')) OR LOWER(p.apellidoMaterno) LIKE LOWER(CONCAT('%', :apellidos, '%'))) AND " +
            "(:telefono IS NULL OR p.telefono LIKE %:telefono%)")
    List<Paciente> buscarAvanzada(@Param("codigo") String codigo,
                                  @Param("documento") String documento,
                                  @Param("nombres") String nombres,
                                  @Param("apellidos") String apellidos,
                                  @Param("telefono") String telefono);

    List<Paciente> findByNombresContainingIgnoreCase(String nombres);

    List<Paciente> findByApellidoPaternoContainingIgnoreCaseOrApellidoMaternoContainingIgnoreCase(String apPaterno, String apMaterno);
}
