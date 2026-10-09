package com.alex.paciente.repository;

import com.alex.paciente.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findAllByOrderByFechaHoraAsc();

    List<Cita> findByPacienteIdOrderByFechaHoraAsc(Long pacienteId);
}
