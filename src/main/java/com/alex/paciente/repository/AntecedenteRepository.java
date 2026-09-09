package com.alex.paciente.repository;

import com.alex.paciente.entity.Antecedente;
import com.alex.paciente.entity.enums.CategoriaAntecedente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AntecedenteRepository extends JpaRepository<Antecedente, Long> {
    List<Antecedente> findByPacienteId(Long pacienteId);
    List<Antecedente> findByPacienteIdAndCategoria(Long pacienteId, CategoriaAntecedente categoria);
}
