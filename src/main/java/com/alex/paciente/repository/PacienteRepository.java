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
 * Repositorio UNIFICADO.
 * - Companero (MVC RF-PAC-03/04): findByCodigoPaciente, findByNumeroDocumento,
 *   buscarGeneral List, buscarAvanzada List.
 * - Req.06 (REST): busquedas por DNI/codigo/nombres/apellidos/telefono/historia
 *   con paginacion + aliases findByDni/findByCodigo para compatibilidad.
 */
@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    // ---------- Companero ----------
    Optional<Paciente> findByCodigoPaciente(String codigoPaciente);

    Optional<Paciente> findByNumeroDocumento(String numeroDocumento);

    boolean existsByNumeroDocumento(String numeroDocumento);

    boolean existsByCodigoPaciente(String codigoPaciente);

    Optional<Paciente> findTopByOrderByIdDesc();

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

    // ---------- Req.06 aliases (compatibilidad API REST: dni=codigo) ----------
    @Query("SELECT p FROM Paciente p WHERE p.numeroDocumento = :dni")
    Optional<Paciente> findByDni(@Param("dni") String dni);

    @Query("SELECT p FROM Paciente p WHERE p.codigoPaciente = :codigo")
    Optional<Paciente> findByCodigo(@Param("codigo") String codigo);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN TRUE ELSE FALSE END FROM Paciente p WHERE p.numeroDocumento = :dni")
    boolean existsByDni(@Param("dni") String dni);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN TRUE ELSE FALSE END FROM Paciente p WHERE p.codigoPaciente = :codigo")
    boolean existsByCodigo(@Param("codigo") String codigo);

    List<Paciente> findByApellidoPaternoContainingIgnoreCase(String apellido);

    @Query("SELECT p FROM Paciente p WHERE " +
            "LOWER(CONCAT(COALESCE(p.apellidoPaterno,''),' ',COALESCE(p.apellidoMaterno,''))) LIKE LOWER(CONCAT('%',:apellidos,'%'))")
    List<Paciente> buscarPorApellidos(@Param("apellidos") String apellidos);

    List<Paciente> findByTelefonoContaining(String telefono);

    Optional<Paciente> findByHistoriaClinica_NumeroHistoria(String numeroHistoria);

    @Query("SELECT DISTINCT p FROM Paciente p LEFT JOIN p.historiaClinica h WHERE " +
            "(:dni IS NULL OR p.numeroDocumento = :dni) AND " +
            "(:codigo IS NULL OR p.codigoPaciente = :codigo) AND " +
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

    @Query("SELECT DISTINCT p FROM Paciente p LEFT JOIN p.historiaClinica h WHERE " +
            "LOWER(p.numeroDocumento) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
            "LOWER(p.codigoPaciente) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
            "LOWER(p.nombres) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
            "LOWER(p.apellidoPaterno) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
            "LOWER(COALESCE(p.apellidoMaterno,'')) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
            "p.telefono LIKE CONCAT('%',:q,'%') OR " +
            "LOWER(h.numeroHistoria) LIKE LOWER(CONCAT('%',:q,'%'))")
    Page<Paciente> busquedaGeneral(@Param("q") String q, Pageable pageable);
}
