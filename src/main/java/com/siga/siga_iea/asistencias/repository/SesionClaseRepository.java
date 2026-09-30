package com.siga.siga_iea.asistencias.repository;

import com.siga.siga_iea.asistencias.entity.SesionClase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SesionClaseRepository extends JpaRepository<SesionClase, UUID> {

    List<SesionClase> findByCursoIdAndFecha(UUID cursoId, LocalDate fecha);

    List<SesionClase> findByCursoIdAndFechaOrderByHoraInicioAsc(UUID cursoId, LocalDate fecha);

    List<SesionClase> findByDocenteIdAndFecha(UUID docenteId, LocalDate fecha);

    List<SesionClase> findByDocenteIdAndFechaOrderByHoraInicioAsc(UUID docenteId, LocalDate fecha);

    Optional<SesionClase> findByCursoIdAndFechaAndHoraInicio(UUID cursoId, LocalDate fecha, LocalTime horaInicio);

    boolean existsByCursoIdAndFechaAndHoraInicio(UUID cursoId, LocalDate fecha, LocalTime horaInicio);

    boolean existsByHorarioIdAndFecha(UUID horarioId, LocalDate fecha);

    @Query("SELECT s FROM SesionClase s WHERE s.curso.id = :cursoId AND s.cursoMateria.materia.id = :materiaId AND s.fecha = :fecha")
    List<SesionClase> findByCursoIdAndMateriaIdAndFecha(@Param("cursoId") UUID cursoId,
                                                        @Param("materiaId") UUID materiaId,
                                                        @Param("fecha") LocalDate fecha);
}
