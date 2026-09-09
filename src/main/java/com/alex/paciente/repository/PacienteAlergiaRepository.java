package com.alex.paciente.repository;

import com.alex.paciente.entity.PacienteAlergia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PacienteAlergiaRepository extends JpaRepository<PacienteAlergia, Long> {
    List<PacienteAlergia> findByPacienteId(Long pacienteId);
    List<PacienteAlergia> findByAlergiaId(Long alergiaId);
    Optional<PacienteAlergia> findByPacienteIdAndAlergiaId(Long pacienteId, Long alergiaId);
    boolean existsByPacienteIdAndAlergiaId(Long pacienteId, Long alergiaId);
    void deleteByPacienteIdAndAlergiaId(Long pacienteId, Long alergiaId);
}
