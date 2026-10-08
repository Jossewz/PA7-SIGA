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
import com.siga.siga_iea.configuracion.entity.AnioLectivo;
import com.siga.siga_iea.configuracion.repository.AnioLectivoRepository;
import com.siga.siga_iea.clases.service.CursoService;
import com.siga.siga_iea.matricula.entity.Matricula;
import com.siga.siga_iea.matricula.repository.MatriculaRepository;
import com.siga.siga_iea.matricula.service.MatriculaService;
import com.siga.siga_iea.auditoria.entity.AuditoriaCambio;
import com.siga.siga_iea.auditoria.repository.AuditoriaCambioRepository;
import com.siga.siga_iea.calificaciones.entity.Calificacion;
import com.siga.siga_iea.calificaciones.entity.Evaluacion;
import com.siga.siga_iea.calificaciones.repository.CalificacionesRepository;
import com.siga.siga_iea.calificaciones.repository.EvaluacionRepository;
import com.siga.siga_iea.calificaciones.service.CalificacionesService;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
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
 * Prueba de integración directa contra PostgreSQL real (Docker).
 * Valida que Hibernate + PostgreSQL JDBC no presenten errores de inferencia
 * de tipo UUID ("could not determine data type of parameter") en las consultas
 * de choques de HorarioRepository y en SesionClaseRepository /
 * AsistenciaRepository.
 *
 * Requiere: Contenedor Docker de PostgreSQL activo en puerto 5433 (siga_test).
 * Ejecutar con: ./mvnw.cmd test -Dgroups=integration
 */
@Tag("integration")
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

        @Autowired
        private AnioLectivoRepository anioLectivoRepository;

        @Autowired
        private MatriculaRepository matriculaRepository;

        @Autowired
        private MatriculaService matriculaService;

        @Autowired
        private CursoService cursoService;

        @Autowired
        private CursoEstudianteRepository cursoEstudianteRepository;

        @Autowired
        private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

        @Autowired
        private AuditoriaCambioRepository auditoriaCambioRepository;

        @Autowired
        private CalificacionesService calificacionesService;

        @Autowired
        private CalificacionesRepository calificacionesRepository;

        @Autowired
        private EvaluacionRepository evaluacionRepository;

        @Autowired
        private org.springframework.transaction.PlatformTransactionManager transactionManager;

        private AnioLectivo obtenerAnioLectivoActual() {
                return anioLectivoRepository.findByEsActualTrue()
                                .orElseGet(() -> anioLectivoRepository.save(new AnioLectivo(2026, "ACTIVO", null, null, true)));
        }

        @Test
        @DisplayName("PostgreSQL: Consultas de solapamiento en HorarioRepository funcionan sin error de tipo UUID")
        void testPostgresHorarioRepositoryQueries() {
                Curso curso = new Curso();
                curso.setGrado("10°");
                curso.setGrupo("01");
                curso.setJornada("Mañana");
                curso.setAnioLectivo(obtenerAnioLectivoActual());
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
                                docente.getId(), "Lunes", "2026", LocalTime.of(8, 0), LocalTime.of(9, 0));
                assertEquals(1, choquesDocente.size(), "PostgreSQL debe encontrar 1 choque de docente");

                // 2. Choque docente con exclusión del propio ID
                List<Horario> choquesExcluido = horarioRepository.buscarSolapamientosDocenteExcluyendoId(
                                docente.getId(), "Lunes", "2026", LocalTime.of(8, 0), LocalTime.of(9, 0), h.getId());
                assertTrue(choquesExcluido.isEmpty(), "PostgreSQL debe excluir el propio ID sin error de tipo UUID");

                // 3. Choque salón sin y con exclusión
                List<Horario> choquesSalon = horarioRepository.buscarSolapamientosSalon(
                                salon.getId(), salon.getCodigo(), "Lunes", "2026", LocalTime.of(7, 30),
                                LocalTime.of(8, 30));
                assertEquals(1, choquesSalon.size());

                List<Horario> choquesSalonExcluido = horarioRepository.buscarSolapamientosSalonExcluyendoId(
                                salon.getId(), salon.getCodigo(), "Lunes", "2026", LocalTime.of(7, 30),
                                LocalTime.of(8, 30), h.getId());
                assertTrue(choquesSalonExcluido.isEmpty());

                // 4. Choque curso sin y con exclusión
                List<Horario> choquesCurso = horarioRepository.buscarSolapamientosCurso(
                                curso.getId(), "Lunes", LocalTime.of(7, 0), LocalTime.of(8, 0));
                assertEquals(1, choquesCurso.size());

                List<Horario> choquesCursoExcluido = horarioRepository.buscarSolapamientosCursoExcluyendoId(
                                curso.getId(), "Lunes", LocalTime.of(7, 0), LocalTime.of(8, 0), h.getId());
                assertTrue(choquesCursoExcluido.isEmpty());
        }

        @Test
        @DisplayName("PostgreSQL: SesionClaseRepository y AsistenciaRepository operan y aplican FK y constraints")
        void testPostgresSesionYAsistenciaRepositories() {
                Curso curso = new Curso();
                curso.setGrado("11°");
                curso.setGrupo("01");
                curso.setJornada("Mañana");
                curso.setAnioLectivo(obtenerAnioLectivoActual());
                curso = cursoRepository.save(curso);

                LocalDate fecha = LocalDate.of(2026, 4, 6);
                SesionClase sesion = new SesionClase(
                                curso, null, null, null, fecha, LocalTime.of(7, 0), LocalTime.of(12, 0), "JORNADA");
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
                curso.setAnioLectivo(obtenerAnioLectivoActual());
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
                assertEquals(1, enPostgres.size(),
                                "PostgreSQL debe contener exactamente 1 fila protegida por la clave única");

                // Limpieza de datos
                sesionClaseRepository.deleteAll(enPostgres);
                horarioRepository.delete(h);
                cursoMateriaRepository.delete(cm);
                docenteRepository.delete(docente);
                materiaRepository.delete(materia);
                cursoRepository.delete(curso);
        }

        @Test
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        @DisplayName("PostgreSQL V18: FK compuesta rechaza curso_materia con anio_lectivo_id distinto al del curso")
        void testPostgresFkCompuestaRechazaDesfaseAnio() {
                AnioLectivo anio2026 = anioLectivoRepository.findByAnio(2026)
                                .orElseGet(() -> anioLectivoRepository.save(new AnioLectivo(2026, "ACTIVO", null, null, true)));
                AnioLectivo anio2027 = anioLectivoRepository.findByAnio(2027)
                                .orElseGet(() -> anioLectivoRepository.save(new AnioLectivo(2027, "FUTURO", null, null, false)));

                Curso curso2026 = new Curso();
                curso2026.setGrado("8°");
                curso2026.setGrupo("01");
                curso2026.setJornada("Mañana");
                curso2026.setAnioLectivo(anio2026);
                curso2026 = cursoRepository.save(curso2026);

                Materia materia = materiaRepository.save(new Materia("Química 8", "QUI-8-" + UUID.randomUUID()));

                UUID cursoId = curso2026.getId();
                UUID materiaId = materia.getId();
                UUID anio2027Id = anio2027.getId();
                UUID cmId = UUID.randomUUID();

                try {
                        // Intento de insertar curso_materia apuntando a curso2026 pero con anio2027Id
                        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> {
                                jdbcTemplate.update(
                                        "INSERT INTO curso_materia (id, curso_id, materia_id, anio_lectivo_id, ano_lectivo) " +
                                        "VALUES (?, ?, ?, ?, '2027')",
                                        cmId, cursoId, materiaId, anio2027Id
                                );
                        }, "La FK compuesta fk_curso_materia_curso_anio debe rechazar un curso_materia con anio diferente a su curso");
                } finally {
                        jdbcTemplate.update("DELETE FROM curso_materia WHERE id = ?", cmId);
                        materiaRepository.delete(materia);
                        cursoRepository.delete(curso2026);
                }
        }

        @Test
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        @DisplayName("PostgreSQL V18: UNIQUE (estudiante_id, anio_lectivo_id) rechaza duplicidad en matriculas y curso_estudiante")
        void testPostgresUniqueEstudianteAnioLectivo() {
                AnioLectivo anio2026 = obtenerAnioLectivoActual();

                Estudiante estudiante = new Estudiante();
                estudiante.setNombres("Gabriel");
                estudiante.setApellidos("García Márquez");
                estudiante.setNumeroDocumento("EST-GABO-" + UUID.randomUUID());
                estudiante.setEstado("Activo");
                estudiante = estudianteRepository.save(estudiante);

                Curso curso = new Curso();
                curso.setGrado("7°");
                curso.setGrupo("01");
                curso.setJornada("Mañana");
                curso.setAnioLectivo(anio2026);
                curso = cursoRepository.save(curso);

                final UUID estudianteId = estudiante.getId();
                final UUID cursoId = curso.getId();
                final UUID anioId = anio2026.getId();

                UUID m1Id = UUID.randomUUID();
                UUID m2Id = UUID.randomUUID();
                UUID ce1Id = UUID.randomUUID();
                UUID ce2Id = UUID.randomUUID();

                try {
                        // 1. UNIQUE en matriculas
                        jdbcTemplate.update(
                                "INSERT INTO matriculas (id, estudiante_id, curso_id, anio_lectivo_id, ano_lectivo, grado, estado, fecha_matricula) " +
                                "VALUES (?, ?, ?, ?, '2026', '7°', 'APROBADA', CURRENT_TIMESTAMP)",
                                m1Id, estudianteId, cursoId, anioId
                        );

                        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> {
                                jdbcTemplate.update(
                                        "INSERT INTO matriculas (id, estudiante_id, curso_id, anio_lectivo_id, ano_lectivo, grado, estado, fecha_matricula) " +
                                        "VALUES (?, ?, ?, ?, '2026', '7°', 'PENDIENTE_DE_REVISION', CURRENT_TIMESTAMP)",
                                        m2Id, estudianteId, cursoId, anioId
                                );
                        }, "La restricción UNIQUE uk_matricula_estudiante_anio_lectivo debe rechazar segunda matrícula en el mismo año");

                        // 2. UNIQUE en curso_estudiante
                        jdbcTemplate.update(
                                "INSERT INTO curso_estudiante (id, curso_id, estudiante_id, anio_lectivo_id, ano_lectivo) " +
                                "VALUES (?, ?, ?, ?, '2026')",
                                ce1Id, cursoId, estudianteId, anioId
                        );

                        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> {
                                jdbcTemplate.update(
                                        "INSERT INTO curso_estudiante (id, curso_id, estudiante_id, anio_lectivo_id, ano_lectivo) " +
                                        "VALUES (?, ?, ?, ?, '2026')",
                                        ce2Id, cursoId, estudianteId, anioId
                                );
                        }, "La restricción UNIQUE uk_curso_estudiante_estudiante_anio_lectivo debe rechazar doble inscripción en el mismo año");
                } finally {
                        jdbcTemplate.update("DELETE FROM matriculas WHERE id IN (?, ?)", m1Id, m2Id);
                        jdbcTemplate.update("DELETE FROM curso_estudiante WHERE id IN (?, ?)", ce1Id, ce2Id);
                        cursoRepository.delete(curso);
                        estudianteRepository.delete(estudiante);
                }
        }

        @Test
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        @DisplayName("PostgreSQL V18: crearCurso asigna esActual por defecto y aprobarMatricula propaga anioLectivo")
        void testPostgresCrearCursoYAprobarMatriculaPropagacion() {
                AnioLectivo anioActual = obtenerAnioLectivoActual();

                // 1. crearCurso toma esActual por defecto cuando anoLectivo es null
                Curso nuevoCurso = cursoService.crearCurso("6°", "98", "Mañana", 30, null, null);
                assertNotNull(nuevoCurso.getId());
                assertNotNull(nuevoCurso.getAnioLectivo(), "crearCurso debe asignar el AnioLectivo activo");
                assertEquals(anioActual.getId(), nuevoCurso.getAnioLectivo().getId());

                // 2. Crear estudiante y matrícula en trámite cumpliendo Ley 1581
                Estudiante estudiante = new Estudiante();
                estudiante.setNombres("Mercedes");
                estudiante.setApellidos("Barcha");
                estudiante.setNumeroDocumento("EST-MER-" + UUID.randomUUID());
                estudiante.setEstado("Pendiente");
                estudiante = estudianteRepository.save(estudiante);

                Matricula mat = new Matricula();
                mat.setEstudiante(estudiante);
                mat.setEstado("PENDIENTE_DE_REVISION");
                mat.setGrado("6°");
                mat.setAnioLectivo(anioActual);
                mat.setAutorizaTratamientoDatos(true);
                mat.setAutorizadoPorNombre("Mercedes Barcha");
                mat.setAutorizadoPorDocumento("DOC-MER-AUTH");
                mat = matriculaRepository.save(mat);

                try {
                        // 3. aprobarMatricula propaga anioLectivo a la matrícula y al registro CursoEstudiante
                        Matricula aprobada = matriculaService.aprobarMatricula(mat.getId(), nuevoCurso.getId());
                        assertEquals("APROBADA", aprobada.getEstado());
                        assertEquals(nuevoCurso.getId(), aprobada.getCurso().getId());
                        assertEquals(anioActual.getId(), aprobada.getAnioLectivo().getId());

                        CursoEstudiante ce = cursoEstudianteRepository.findByCursoIdAndEstudianteIdAndAnoLectivo(
                                        nuevoCurso.getId(), estudiante.getId(), "2026")
                                        .orElseThrow(() -> new AssertionError("CursoEstudiante debe existir tras aprobación"));
                        assertNotNull(ce.getAnioLectivo());
                        assertEquals(anioActual.getId(), ce.getAnioLectivo().getId(), "aprobarMatricula debe propagar el anioLectivo a CursoEstudiante");
                } finally {
                        cursoEstudianteRepository.findByEstudianteId(estudiante.getId())
                                        .forEach(cursoEstudianteRepository::delete);
                        matriculaRepository.delete(mat);
                        estudianteRepository.delete(estudiante);
                        cursoRepository.delete(nuevoCurso);
                }
        }

        @Test
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        @DisplayName("PostgreSQL V19: chk_matricula_curso_aprobada rechaza UPDATE a APROBADA con curso_id NULL")
        void testPostgresV19CheckMatriculaCursoAprobada() {
                AnioLectivo anioActual = obtenerAnioLectivoActual();

                Estudiante estudiante = new Estudiante();
                estudiante.setNombres("Jorge");
                estudiante.setApellidos("Isaacs");
                estudiante.setNumeroDocumento("EST-ISA-" + UUID.randomUUID());
                estudiante.setEstado("Pendiente");
                estudiante = estudianteRepository.save(estudiante);

                final UUID estudianteId = estudiante.getId();
                final UUID anioId = anioActual.getId();
                UUID matId = UUID.randomUUID();
                Curso curso = null;

                try {
                        // Permitido por V19: curso_id = NULL cuando estado = 'PENDIENTE_DE_REVISION'
                        jdbcTemplate.update(
                                "INSERT INTO matriculas (id, estudiante_id, curso_id, anio_lectivo_id, ano_lectivo, grado, estado, fecha_matricula) " +
                                "VALUES (?, ?, NULL, ?, '2026', '10°', 'PENDIENTE_DE_REVISION', CURRENT_TIMESTAMP)",
                                matId, estudianteId, anioId
                        );

                        // Rechazado por V19: UPDATE a APROBADA manteniendo curso_id NULL
                        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> {
                                jdbcTemplate.update(
                                        "UPDATE matriculas SET estado = 'APROBADA' WHERE id = ?",
                                        matId
                                );
                        }, "chk_matricula_curso_aprobada debe rechazar una matrícula APROBADA sin curso_id");

                        // Permitido por V19: cuando se asigna un curso en la misma operación
                        curso = new Curso();
                        curso.setGrado("10°");
                        curso.setGrupo("97");
                        curso.setJornada("Tarde");
                        curso.setAnioLectivo(anioActual);
                        curso = cursoRepository.save(curso);

                        int updated = jdbcTemplate.update(
                                "UPDATE matriculas SET estado = 'APROBADA', curso_id = ? WHERE id = ?",
                                curso.getId(), matId
                        );
                        assertEquals(1, updated, "UPDATE a APROBADA con curso_id asignado debe permitirse");
                } finally {
                        jdbcTemplate.update("DELETE FROM matriculas WHERE id = ?", matId);
                        if (curso != null) {
                                cursoRepository.delete(curso);
                        }
                        estudianteRepository.delete(estudiante);
                }
        }

        @Test
        @DisplayName("PostgreSQL V18/V19: Integridad de datos - ningún anio_lectivo_id es nulo en el esquema")
        void testPostgresNoNullAnioLectivoEnTablasPrincipales() {
                Integer cursosNulos = jdbcTemplate.queryForObject(
                        "SELECT count(*) FROM cursos WHERE anio_lectivo_id IS NULL", Integer.class);
                assertEquals(0, cursosNulos, "No debe haber cursos con anio_lectivo_id nulo");

                Integer cmNulos = jdbcTemplate.queryForObject(
                        "SELECT count(*) FROM curso_materia WHERE anio_lectivo_id IS NULL", Integer.class);
                assertEquals(0, cmNulos, "No debe haber curso_materia con anio_lectivo_id nulo");

                Integer ceNulos = jdbcTemplate.queryForObject(
                        "SELECT count(*) FROM curso_estudiante WHERE anio_lectivo_id IS NULL", Integer.class);
                assertEquals(0, ceNulos, "No debe haber curso_estudiante con anio_lectivo_id nulo");

                Integer matNulos = jdbcTemplate.queryForObject(
                        "SELECT count(*) FROM matriculas WHERE anio_lectivo_id IS NULL", Integer.class);
                assertEquals(0, matNulos, "No debe haber matriculas con anio_lectivo_id nulo");
        }

        @Test
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        @DisplayName("PostgreSQL V20: Auditoría de calificaciones (append-only, sin huérfanos y usuario registrado)")
        void testPostgresV20AuditoriaCambioCalificacion() {
                Curso curso = new Curso();
                curso.setGrado("10°");
                curso.setGrupo("02");
                curso.setJornada("Mañana");
                curso.setAnioLectivo(obtenerAnioLectivoActual());
                curso = cursoRepository.save(curso);

                Materia materia = new Materia("Física 10", "FIS-10-" + UUID.randomUUID());
                materia = materiaRepository.save(materia);

                Docente docente = new Docente();
                docente.setNombres("Albert");
                docente.setApellidos("Einstein");
                docente.setNumeroDocumento("DOC-ALB-" + UUID.randomUUID());
                docente.setEstado("Activo");
                docente = docenteRepository.save(docente);

                CursoMateria cm = new CursoMateria(curso, materia, docente, "2026");
                cm = cursoMateriaRepository.save(cm);

                Evaluacion ev = new Evaluacion(cm, "Taller de Dinámica", 1, new BigDecimal("30.00"));
                ev = evaluacionRepository.save(ev);

                Estudiante est = new Estudiante();
                est.setNombres("Isaac");
                est.setApellidos("Newton");
                est.setNumeroDocumento("EST-ISAAC-" + UUID.randomUUID());
                est.setEstado("Activo");
                est = estudianteRepository.save(est);

                try {
                        // 1. Registro inicial de nota: 3.50 -> exactamente 1 registro
                        Calificacion c1 = calificacionesService.registrarONota(ev.getId(), est.getId(), new BigDecimal("3.50"), "Nota inicial");
                        assertNotNull(c1.getId());

                        List<AuditoriaCambio> logs1 = auditoriaCambioRepository.findByEntidadAndEntidadIdOrderByFechaDesc("CALIFICACION", c1.getId());
                        assertEquals(1, logs1.size(), "Debe existir exactamente 1 registro de auditoría inicial");
                        assertEquals("SIN_NOTA", logs1.get(0).getValorAnterior());
                        assertEquals("3.50", logs1.get(0).getValorNuevo());
                        assertEquals("nota", logs1.get(0).getCampo());
                        assertNotNull(logs1.get(0).getUsuarioEmail(), "El contexto del usuario debe quedar registrado");

                        // 2. Modificación de nota: 4.80 -> exactamente una segunda fila
                        Calificacion c2 = calificacionesService.registrarONota(ev.getId(), est.getId(), new BigDecimal("4.80"), "Corrección docente");
                        List<AuditoriaCambio> logs2 = auditoriaCambioRepository.findByEntidadAndEntidadIdOrderByFechaDesc("CALIFICACION", c2.getId());
                        assertEquals(2, logs2.size(), "Deben existir exactamente 2 registros tras una segunda modificación");
                        assertEquals("3.50", logs2.get(0).getValorAnterior(), "El valor anterior debe ser 3.50");
                        assertEquals("4.80", logs2.get(0).getValorNuevo(), "El valor nuevo debe ser 4.80");

                        // 3. Regla append-only: UPDATE o DELETE directo debe ser bloqueado por el trigger en PostgreSQL
                        assertThrows(org.springframework.dao.DataAccessException.class, () -> {
                                jdbcTemplate.update("UPDATE auditoria_cambios SET valor_nuevo = '9.99' WHERE id = ?", logs1.get(0).getId());
                        }, "PostgreSQL debe rechazar UPDATE sobre auditoria_cambios debido al trigger append-only");

                        assertThrows(org.springframework.dao.DataAccessException.class, () -> {
                                jdbcTemplate.update("DELETE FROM auditoria_cambios WHERE id = ?", logs1.get(0).getId());
                        }, "PostgreSQL debe rechazar DELETE sobre auditoria_cambios debido al trigger append-only");

                        // 4. Integridad transaccional: una transacción que falla y hace rollback NO debe dejar auditoría huérfana
                        final UUID evId = ev.getId();
                        final UUID estId = est.getId();
                        org.springframework.transaction.support.TransactionTemplate tt =
                                new org.springframework.transaction.support.TransactionTemplate(transactionManager);
                        assertThrows(RuntimeException.class, () -> {
                                tt.execute(status -> {
                                        calificacionesService.registrarONota(evId, estId, new BigDecimal("1.00"), "Nota abortada");
                                        throw new RuntimeException("Fallo intencional para verificar rollback");
                                });
                        });

                        List<AuditoriaCambio> logsPostRollback = auditoriaCambioRepository.findByEntidadAndEntidadIdOrderByFechaDesc("CALIFICACION", c2.getId());
                        assertEquals(2, logsPostRollback.size(), "La auditoría no debe dejar registros huérfanos cuando la transacción principal hace rollback");

                } finally {
                        // Limpieza segura (desactivando temporalmente el trigger append-only para el cleanup de tests)
                        final UUID cleanEvId = ev.getId();
                        final UUID cleanEstId = est.getId();
                        calificacionesRepository.findByEvaluacionIdAndEstudianteId(cleanEvId, cleanEstId)
                                .ifPresent(c -> {
                                        jdbcTemplate.execute("ALTER TABLE auditoria_cambios DISABLE TRIGGER trg_auditoria_cambios_bloquear_modificacion");
                                        List<AuditoriaCambio> logs = auditoriaCambioRepository.findByEntidadAndEntidadIdOrderByFechaDesc("CALIFICACION", c.getId());
                                        auditoriaCambioRepository.deleteAll(logs);
                                        jdbcTemplate.execute("ALTER TABLE auditoria_cambios ENABLE TRIGGER trg_auditoria_cambios_bloquear_modificacion");
                                        calificacionesRepository.delete(c);
                                });
                        evaluacionRepository.delete(ev);
                        cursoMateriaRepository.delete(cm);
                        docenteRepository.delete(docente);
                        materiaRepository.delete(materia);
                        cursoRepository.delete(curso);
                        estudianteRepository.delete(est);
                }
        }
}

