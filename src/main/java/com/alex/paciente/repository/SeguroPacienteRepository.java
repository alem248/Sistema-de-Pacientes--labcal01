package com.alex.paciente.repository;

import com.alex.paciente.entity.SeguroPaciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeguroPacienteRepository extends JpaRepository<SeguroPaciente, Long> {
    List<SeguroPaciente> findByPacienteId(Long pacienteId);
}
