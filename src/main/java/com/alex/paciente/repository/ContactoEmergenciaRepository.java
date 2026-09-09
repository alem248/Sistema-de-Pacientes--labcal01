package com.alex.paciente.repository;

import com.alex.paciente.entity.ContactoEmergencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactoEmergenciaRepository extends JpaRepository<ContactoEmergencia, Long> {
    List<ContactoEmergencia> findByPacienteId(Long pacienteId);
    List<ContactoEmergencia> findByPacienteIdAndEsPrincipalTrue(Long pacienteId);
}
