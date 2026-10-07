package com.alex.paciente.repository;

import com.alex.paciente.entity.Auditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    Page<Auditoria> findAllByOrderByFechaHoraDesc(Pageable pageable);

    List<Auditoria> findByEntidadAndEntidadIdOrderByFechaHoraDesc(String entidad, String entidadId);

    List<Auditoria> findByUsuarioOrderByFechaHoraDesc(String usuario);

    List<Auditoria> findByOperacionOrderByFechaHoraDesc(String operacion);
}
