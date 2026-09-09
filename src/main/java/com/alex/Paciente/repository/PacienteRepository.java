package com.alex.Paciente.repository;

import com.alex.Paciente.entity.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * RF-PAC-02: Validacion del numero de documento
 * Verifica que el numero de documento no exista previamente en MySQL
 */
@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    /**
     * Verifica si existe un paciente con el numero de documento dado
     * @param numeroDocumento numero de documento a verificar
     * @return true si existe, false si no
     */
    boolean existsByNumeroDocumento(String numeroDocumento);

    /**
     * Busca paciente por numero de documento
     */
    Optional<Paciente> findByNumeroDocumento(String numeroDocumento);
}
