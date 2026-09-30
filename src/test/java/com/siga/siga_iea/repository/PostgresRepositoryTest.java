package com.siga.siga_iea.repository;

import com.siga.siga_iea.asistencias.entity.Asistencia;
import com.siga.siga_iea.asistencias.entity.SesionClase;
import com.siga.siga_iea.asistencias.repository.AsistenciaRepository;
import com.siga.siga_iea.asistencias.repository.SesionClaseRepository;
import com.siga.siga_iea.asistencias.service.AsistenciaService;
import com.siga.siga_iea.clases.entity.*;
import com.siga.siga_iea.clases.repository.*;
import com.siga.siga_iea.usuarios.entity.Docente;
import com.siga.siga_iea.usuarios.entity.Estudiante;
import com.siga.siga_iea.usuarios.repository.DocenteRepository;
import com.siga.siga_iea.usuarios.repository.EstudianteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba de integración directa contra PostgreSQL 16 real (Docker).
 * Valida que Hibernate + PostgreSQL JDBC no presenten errores de inferencia
 * de tipo UUID ("could not determine data type of parameter") en las consultas
 * de choques de HorarioRepository y en SesionClaseRepository / AsistenciaRepository.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5433/siga_test",
        "spring.datasource.username=postgres",
        "spring.datasource.password=siga",
        "spring.datasource.driver-class-name=org.postgresql.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true"
})
@Transactional
class PostgresRepositoryTest {

    @Autowired
    private AsistenciaService asistenciaService;

    @Autowired
    private HorarioRepository horarioRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private MateriaRepository materiaRepository;

    @Autowired
    private DocenteRepository docenteRepository;

    @Autowired
    private SalonRepository salonRepository;

    @Autowired
    private CursoMateriaRepository cursoMateriaRepository;

    @Autowired
    private SesionClaseRepository sesionClaseRepository;

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Test
    @DisplayName("PostgreSQL: Consultas de solapamiento en HorarioRepository funcionan sin error de tipo UUID")
    void testPostgresHorarioRepositoryQueries() {
        Curso curso = new Curso();
        curso.setGrado("10°");
        curso.setGrupo("01");
        curso.setJornada("Mañana");
        curso.setAnoLectivo("2026");
        curso = cursoRepository.save(curso);

        Materia materia = new Materia("Filosofía 10", "FIL-10-" + UUID.randomUUID());
        materia = materiaRepository.save(materia);

        Docente docente = new Docente();
        docente.setNombres("Sócrates");
        docente.setApellidos("Atenas");
        docente.setNumeroDocumento("DOC-SOC-" + UUID.randomUUID());
        docente.setEstado("Activo");
        docente = docenteRepository.save(docente);

        CursoMateria cm = new CursoMateria(curso, materia, docente, "2026");
        cursoMateriaRepository.save(cm);

        Salon salon = new Salon("AULA-PG-101", "Aula Postgres 101", 35, "Bloque PG", "AULA", "Activo");
        salon = salonRepository.save(salon);

        Horario h = new Horario();
        h.setCurso(curso);
        h.setMateria(materia);
        h.setSalonEntidad(salon);
        h.setSalon(salon.getCodigo());
        h.setDiaSemana("Lunes");
        h.setHoraInicio(LocalTime.of(7, 0));
        h.setHoraFin(LocalTime.of(8, 30));
        h = horarioRepository.save(h);

        // 1. Choque docente sin exclusión
        List<Horario> choquesDocente = horarioRepository.buscarSolapamientosDocente(
                docente.getId(), "Lunes", "2026", LocalTime.of(8, 0), LocalTime.of(9, 0)
        );
        assertEquals(1, choquesDocente.size(), "PostgreSQL debe encontrar 1 choque de docente");

        // 2. Choque docente con exclusión del propio ID
        List<Horario> choquesExcluido = horarioRepository.buscarSolapamientosDocenteExcluyendoId(
                docente.getId(), "Lunes", "2026", LocalTime.of(8, 0), LocalTime.of(9, 0), h.getId()
        );
        assertTrue(choquesExcluido.isEmpty(), "PostgreSQL debe excluir el propio ID sin error de tipo UUID");

        // 3. Choque salón sin y con exclusión
        List<Horario> choquesSalon = horarioRepository.buscarSolapamientosSalon(
                salon.getId(), salon.getCodigo(), "Lunes", "2026", LocalTime.of(7, 30), LocalTime.of(8, 30)
        );
        assertEquals(1, choquesSalon.size());

        List<Horario> choquesSalonExcluido = horarioRepository.buscarSolapamientosSalonExcluyendoId(
                salon.getId(), salon.getCodigo(), "Lunes", "2026", LocalTime.of(7, 30), LocalTime.of(8, 30), h.getId()
        );
        assertTrue(choquesSalonExcluido.isEmpty());

        // 4. Choque curso sin y con exclusión
        List<Horario> choquesCurso = horarioRepository.buscarSolapamientosCurso(
                curso.getId(), "Lunes", LocalTime.of(7, 0), LocalTime.of(8, 0)
        );
        assertEquals(1, choquesCurso.size());

        List<Horario> choquesCursoExcluido = horarioRepository.buscarSolapamientosCursoExcluyendoId(
                curso.getId(), "Lunes", LocalTime.of(7, 0), LocalTime.of(8, 0), h.getId()
        );
        assertTrue(choquesCursoExcluido.isEmpty());
    }

    @Test
    @DisplayName("PostgreSQL: SesionClaseRepository y AsistenciaRepository operan y aplican FK y constraints")
    void testPostgresSesionYAsistenciaRepositories() {
        Curso curso = new Curso();
        curso.setGrado("11°");
        curso.setGrupo("01");
        curso.setJornada("Mañana");
        curso.setAnoLectivo("2026");
        curso = cursoRepository.save(curso);

        LocalDate fecha = LocalDate.of(2026, 4, 6);
        SesionClase sesion = new SesionClase(
                curso, null, null, null, fecha, LocalTime.of(7, 0), LocalTime.of(12, 0), "JORNADA"
        );
        sesion.setEstado("DICTADA");
        sesion = sesionClaseRepository.save(sesion);
        assertNotNull(sesion.getId());

        Estudiante est = new Estudiante();
        est.setNombres("Alejandro");
        est.setApellidos("Magno");
        est.setNumeroDocumento("EST-PG-" + UUID.randomUUID());
        est.setEstado("Activo");
        est = estudianteRepository.save(est);

        Asistencia asistencia = new Asistencia(sesion, est, "PRESENTE", "Registro en PostgreSQL real");
        asistencia = asistenciaRepository.save(asistencia);
        assertNotNull(asistencia.getId());

        List<Asistencia> asistencias = asistenciaRepository.findBySesionId(sesion.getId());
        assertEquals(1, asistencias.size());
        assertEquals("PRESENTE", asistencias.get(0).getEstado());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @DisplayName("PostgreSQL: Concurrencia real en abrirDia previene duplicados mediante uk_sesion_curso_fecha_hora")
    void testPostgresConcurrenciaAbrirDia() throws Exception {
        Curso curso = new Curso();
        curso.setGrado("9°");
        curso.setGrupo("01");
        curso.setJornada("Mañana");
        curso.setAnoLectivo("2026");
        curso = cursoRepository.save(curso);

        Materia materia = new Materia("Biología 9", "BIO-9-" + UUID.randomUUID());
        materia = materiaRepository.save(materia);

        Docente docente = new Docente();
        docente.setNombres("Charles");
        docente.setApellidos("Darwin");
        docente.setNumeroDocumento("DOC-DAR-" + UUID.randomUUID());
        docente.setEstado("Activo");
        docente = docenteRepository.save(docente);

        CursoMateria cm = new CursoMateria(curso, materia, docente, "2026");
        cursoMateriaRepository.save(cm);

        Horario h = new Horario();
        h.setCurso(curso);
        h.setMateria(materia);
        h.setDiaSemana("Miércoles");
        h.setHoraInicio(LocalTime.of(8, 0));
        h.setHoraFin(LocalTime.of(9, 30));
        horarioRepository.save(h);

        LocalDate miercoles = LocalDate.of(2026, 3, 4);

        int hilos = 4;
        ExecutorService executor = Executors.newFixedThreadPool(hilos);
        CountDownLatch latch = new CountDownLatch(1);
        List<Future<List<SesionClase>>> futures = new ArrayList<>();

        for (int i = 0; i < hilos; i++) {
            final UUID cId = curso.getId();
            futures.add(executor.submit(() -> {
                latch.await();
                return asistenciaService.abrirDia(cId, miercoles);
            }));
        }

        latch.countDown();

        for (Future<List<SesionClase>> f : futures) {
            List<SesionClase> res = f.get(10, TimeUnit.SECONDS);
            assertEquals(1, res.size(), "Cada hilo debe recibir 1 sesión");
        }
        executor.shutdown();

        List<SesionClase> enPostgres = sesionClaseRepository.findByCursoIdAndFecha(curso.getId(), miercoles);
        assertEquals(1, enPostgres.size(), "PostgreSQL debe contener exactamente 1 fila protegida por la clave única");

        // Limpieza de datos
        sesionClaseRepository.deleteAll(enPostgres);
        horarioRepository.delete(h);
        cursoMateriaRepository.delete(cm);
        docenteRepository.delete(docente);
        materiaRepository.delete(materia);
        cursoRepository.delete(curso);
    }
}
