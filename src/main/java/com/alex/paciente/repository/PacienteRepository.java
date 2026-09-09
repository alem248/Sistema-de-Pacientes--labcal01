package com.alex.paciente.repository;

import com.alex.paciente.entity.Paciente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Req.6 - Búsqueda de pacientes por DNI, código, nombres, apellidos, teléfono e historia clínica.
 */
@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    Optional<Paciente> findByDni(String dni);

    Optional<Paciente> findByCodigo(String codigo);

    List<Paciente> findByNombresContainingIgnoreCase(String nombres);

    List<Paciente> findByApellidoPaternoContainingIgnoreCase(String apellido);

    @Query("SELECT p FROM Paciente p WHERE " +
            "LOWER(CONCAT(COALESCE(p.apellidoPaterno,''),' ',COALESCE(p.apellidoMaterno,''))) LIKE LOWER(CONCAT('%',:apellidos,'%'))")
    List<Paciente> buscarPorApellidos(@Param("apellidos") String apellidos);

    List<Paciente> findByTelefonoContaining(String telefono);

    Optional<Paciente> findByHistoriaClinica_NumeroHistoria(String numeroHistoria);

    boolean existsByDni(String dni);

    boolean existsByCodigo(String codigo);

    /**
     * Búsqueda avanzada combinada con filtros opcionales + paginación.
     */
    @Query("SELECT DISTINCT p FROM Paciente p LEFT JOIN p.historiaClinica h WHERE " +
            "(:dni IS NULL OR p.dni = :dni) AND " +
            "(:codigo IS NULL OR p.codigo = :codigo) AND " +
            "(:nombres IS NULL OR LOWER(p.nombres) LIKE LOWER(CONCAT('%',:nombres,'%'))) AND " +
            "(:apellidos IS NULL OR LOWER(CONCAT(COALESCE(p.apellidoPaterno,''),' ',COALESCE(p.apellidoMaterno,''))) LIKE LOWER(CONCAT('%',:apellidos,'%'))) AND " +
            "(:telefono IS NULL OR p.telefono LIKE CONCAT('%',:telefono,'%')) AND " +
            "(:numeroHistoria IS NULL OR h.numeroHistoria = :numeroHistoria)")
    Page<Paciente> busquedaAvanzada(@Param("dni") String dni,
                                   @Param("codigo") String codigo,
                                   @Param("nombres") String nombres,
                                   @Param("apellidos") String apellidos,
                                   @Param("telefono") String telefono,
                                   @Param("numeroHistoria") String numeroHistoria,
                                   Pageable pageable);

    /**
     * Búsqueda general: un solo texto contra DNI, código, nombres, apellidos, teléfono e historia.
     */
    @Query("SELECT DISTINCT p FROM Paciente p LEFT JOIN p.historiaClinica h WHERE " +
            "LOWER(p.dni) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
            "LOWER(p.codigo) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
            "LOWER(p.nombres) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
            "LOWER(p.apellidoPaterno) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
            "LOWER(COALESCE(p.apellidoMaterno,'')) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
            "p.telefono LIKE CONCAT('%',:q,'%') OR " +
            "LOWER(h.numeroHistoria) LIKE LOWER(CONCAT('%',:q,'%'))")
    Page<Paciente> busquedaGeneral(@Param("q") String q, Pageable pageable);
}
