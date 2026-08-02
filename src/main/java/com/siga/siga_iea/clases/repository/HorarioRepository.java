package com.siga.siga_iea.clases.repository;

import com.siga.siga_iea.clases.entity.Horario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HorarioRepository extends JpaRepository<Horario, UUID> {
    List<Horario> findByCursoId(UUID cursoId);
}
