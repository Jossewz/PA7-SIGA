package com.siga.siga_iea.asistencias.repository;

import com.siga.siga_iea.asistencias.entity.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, UUID> {

    List<Asistencia> findByCursoIdAndFecha(UUID cursoId, LocalDate fecha);

    List<Asistencia> findByCursoIdAndMateriaIdAndFecha(UUID cursoId, UUID materiaId, LocalDate fecha);

    Optional<Asistencia> findByCursoIdAndEstudianteIdAndFechaAndMateriaId(UUID cursoId, UUID estudianteId, LocalDate fecha, UUID materiaId);

    Optional<Asistencia> findByCursoIdAndEstudianteIdAndFecha(UUID cursoId, UUID estudianteId, LocalDate fecha);

    List<Asistencia> findByEstudianteId(UUID estudianteId);
}
