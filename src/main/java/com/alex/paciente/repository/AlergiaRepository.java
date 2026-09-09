package com.alex.paciente.repository;

import com.alex.paciente.entity.Alergia;
import com.alex.paciente.enums.TipoAlergia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlergiaRepository extends JpaRepository<Alergia, Long> {
    Optional<Alergia> findByNombreIgnoreCase(String nombre);
    List<Alergia> findByTipo(TipoAlergia tipo);
    boolean existsByNombreIgnoreCase(String nombre);
}
