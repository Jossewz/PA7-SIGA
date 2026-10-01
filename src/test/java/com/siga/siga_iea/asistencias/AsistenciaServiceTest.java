package com.siga.siga_iea.asistencias;

import com.siga.siga_iea.asistencias.dto.AsistenciaItemDto;
import com.siga.siga_iea.asistencias.entity.Asistencia;
import com.siga.siga_iea.asistencias.entity.SesionClase;
import com.siga.siga_iea.asistencias.repository.AsistenciaRepository;
import com.siga.siga_iea.asistencias.repository.SesionClaseRepository;
import com.siga.siga_iea.asistencias.service.AsistenciaService;
import com.siga.siga_iea.auth.enums.RolEnum;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AsistenciaServiceTest {

    @Autowired
    private AsistenciaService asistenciaService;

    @Autowired
    private SesionClaseRepository sesionClaseRepository;

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private MateriaRepository materiaRepository;

    @Autowired
    private DocenteRepository docenteRepository;

    @Autowired
    private CursoMateriaRepository cursoMateriaRepository;

    @Autowired
    private CursoEstudianteRepository cursoEstudianteRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private HorarioRepository horarioRepository;

    private Curso crearCurso(String grado, String grupo, String jornada, String anoLectivo) {
        Curso c = new Curso();
        c.setGrado(grado);
        c.setGrupo(grupo);
        c.setJornada(jornada);
        c.setAnoLectivo(anoLectivo);
        return cursoRepository.save(c);
    }

    private Materia crearMateria(String nombre, String codigo) {
        Materia m = new Materia(nombre, codigo);
        return materiaRepository.save(m);
    }

    private Docente crearDocente(String nombres, String apellidos, String documento) {
        Docente d = new Docente();
        d.setNombres(nombres);
        d.setApellidos(apellidos);
        d.setNumeroDocumento(documento);
        d.setEstado("Activo");
        return docenteRepository.save(d);
    }

    private Estudiante crearEstudiante(String nombres, String apellidos, String documento) {
        Estudiante e = new Estudiante();
        e.setNombres(nombres);
        e.setApellidos(apellidos);
        e.setNumeroDocumento(documento);
        e.setEstado("Activo");
        return estudianteRepository.save(e);
    }

    @Test
    @Transactional
    @DisplayName("Prueba 1: abrirDia dos veces es idempotente y no duplica sesiones")
    void testAbrirDiaEsIdempotenteNoDuplicaSesiones() {
        Curso curso = crearCurso("10°", "01", "Mañana", "2026");
        Materia mat = crearMateria("Matemáticas 10", "MAT-10-" + UUID.randomUUID());
        Docente doc = crearDocente("Juan", "Pérez", "DOC-001-" + UUID.randomUUID());

        cursoMateriaRepository.save(new CursoMateria(curso, mat, doc, "2026"));

        Horario h = new Horario();
        h.setCurso(curso);
        h.setMateria(mat);
        h.setDiaSemana("Lunes");
        h.setHoraInicio(LocalTime.of(7, 0));
        h.setHoraFin(LocalTime.of(8, 30));
        horarioRepository.save(h);

        LocalDate lunes = LocalDate.of(2026, 3, 2); // 2026-03-02 es Lunes

        // Primera apertura
        List<SesionClase> sesion1 = asistenciaService.abrirDia(curso.getId(), lunes);
        assertEquals(1, sesion1.size());
        assertEquals(LocalTime.of(7, 0), sesion1.get(0).getHoraInicio());

        // Segunda apertura
        List<SesionClase> sesion2 = asistenciaService.abrirDia(curso.getId(), lunes);
        assertEquals(1, sesion2.size());
        assertEquals(sesion1.get(0).getId(), sesion2.get(0).getId(), "Debe retornar la misma sesión existente");

        // Conteo en BD
        List<SesionClase> enBd = sesionClaseRepository.findByCursoIdAndFecha(curso.getId(), lunes);
        assertEquals(1, enBd.size(), "No deben existir sesiones duplicadas en BD");
    }

    @Test
    @Transactional
    @DisplayName("Prueba 2: Sesiones generadas para secundaria copian el docente desde CursoMateria, no desde Horario")
    void testDocenteSeCopiaDeCursoMateriaYNoDeHorario() {
        Curso curso = crearCurso("11°", "02", "Mañana", "2026");
        Materia mat = crearMateria("Física 11", "FIS-11-" + UUID.randomUUID());

        Docente docenteOficialCM = crearDocente("María", "Rodríguez", "DOC-OFICIAL-" + UUID.randomUUID());
        Docente docenteResidualHorario = crearDocente("Pedro", "Gómez", "DOC-RESIDUAL-" + UUID.randomUUID());

        // En CursoMateria el titular oficial es María Rodríguez
        cursoMateriaRepository.save(new CursoMateria(curso, mat, docenteOficialCM, "2026"));

        // En Horario residual quedó Pedro Gómez
        Horario h = new Horario();
        h.setCurso(curso);
        h.setMateria(mat);
        h.setDocente(docenteResidualHorario); // Residual que debe ser ignorado
        h.setDiaSemana("Martes");
        h.setHoraInicio(LocalTime.of(8, 30));
        h.setHoraFin(LocalTime.of(10, 0));
        horarioRepository.save(h);

        LocalDate martes = LocalDate.of(2026, 3, 3); // Martes

        List<SesionClase> sesiones = asistenciaService.abrirDia(curso.getId(), martes);
        assertEquals(1, sesiones.size());

        SesionClase sesion = sesiones.get(0);
        assertNotNull(sesion.getDocente(), "La sesión debe tener docente asignado");
        assertEquals(docenteOficialCM.getId(), sesion.getDocente().getId(),
                "El docente de la sesión debe ser copiado estrictamente de CursoMateria, no de Horario");
    }

    @Test
    @Transactional
    @DisplayName("Prueba 3: Primaria (Transición a 5°) crea una sola sesión tipo JORNADA sin materia y con jornada completa")
    void testPrimariaCreaSesionJornadaSinMateria() {
        Curso cursoPrimaria = crearCurso("3°", "01", "Mañana", "2026");
        Docente director = crearDocente("Directora", "Primaria", "DOC-DIR-" + UUID.randomUUID());
        cursoPrimaria.setDirector(director);
        cursoRepository.save(cursoPrimaria);

        LocalDate fecha = LocalDate.of(2026, 3, 4); // Miércoles

        List<SesionClase> sesiones = asistenciaService.abrirDia(cursoPrimaria.getId(), fecha);
        assertEquals(1, sesiones.size(), "Primaria debe tener exactamente 1 sesión por día");

        SesionClase sesion = sesiones.get(0);
        assertEquals("JORNADA", sesion.getTipo(), "El tipo de sesión debe ser JORNADA");
        assertNull(sesion.getCursoMateria(), "La sesión JORNADA no tiene materia asignada");
        assertEquals(LocalTime.of(7, 0), sesion.getHoraInicio(), "Debe iniciar con la jornada completa (07:00)");
        assertEquals(LocalTime.of(12, 0), sesion.getHoraFin(), "Debe terminar con la jornada completa (12:00)");
        assertEquals(director.getId(), sesion.getDocente().getId(), "Debe asignar al director de grupo");

        // Idempotencia en primaria
        List<SesionClase> reabierta = asistenciaService.abrirDia(cursoPrimaria.getId(), fecha);
        assertEquals(1, reabierta.size());
        assertEquals(sesion.getId(), reabierta.get(0).getId());
    }

    @Test
    @Transactional
    @DisplayName("Prueba 4: No se puede registrar asistencia de un estudiante que no está en el curso (CursoEstudiante)")
    void testRegistrarRechazaEstudianteNoInscritoEnCurso() {
        Curso curso = crearCurso("9°", "01", "Mañana", "2026");
        Materia mat = crearMateria("Inglés 9", "ING-9-" + UUID.randomUUID());
        Docente doc = crearDocente("Teacher", "John", "DOC-ENG-" + UUID.randomUUID());
        cursoMateriaRepository.save(new CursoMateria(curso, mat, doc, "2026"));

        Horario h = new Horario();
        h.setCurso(curso);
        h.setMateria(mat);
        h.setDiaSemana("Jueves");
        h.setHoraInicio(LocalTime.of(7, 0));
        h.setHoraFin(LocalTime.of(8, 30));
        horarioRepository.save(h);

        LocalDate jueves = LocalDate.of(2026, 3, 5); // Jueves
        List<SesionClase> sesiones = asistenciaService.abrirDia(curso.getId(), jueves);
        UUID sesionId = sesiones.get(0).getId();

        // Estudiante no inscrito en el curso
        Estudiante estIntruso = crearEstudiante("Carlos", "Intruso", "EST-INT-" + UUID.randomUUID());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                asistenciaService.registrar(sesionId, List.of(new AsistenciaItemDto(estIntruso.getId(), "PRESENTE"))));

        assertTrue(ex.getMessage().contains("no está inscrito en el curso"),
                "El mensaje de error debe indicar que el estudiante no está inscrito");
    }

    @Test
    @Transactional
    @DisplayName("Prueba 5: Registrar dos veces el mismo estudiante actualiza el registro (upsert), no lo duplica")
    void testRegistrarHaceUpsertNoDuplicaMismoEstudiante() {
        Curso curso = crearCurso("8°", "01", "Mañana", "2026");
        Materia mat = crearMateria("Biología 8", "BIO-8-" + UUID.randomUUID());
        Docente doc = crearDocente("Biólogo", "Ramírez", "DOC-BIO-" + UUID.randomUUID());
        cursoMateriaRepository.save(new CursoMateria(curso, mat, doc, "2026"));

        Horario h = new Horario();
        h.setCurso(curso);
        h.setMateria(mat);
        h.setDiaSemana("Viernes");
        h.setHoraInicio(LocalTime.of(9, 0));
        h.setHoraFin(LocalTime.of(10, 30));
        horarioRepository.save(h);

        LocalDate viernes = LocalDate.of(2026, 3, 6); // Viernes
        List<SesionClase> sesiones = asistenciaService.abrirDia(curso.getId(), viernes);
        UUID sesionId = sesiones.get(0).getId();

        // Inscribir estudiante legalmente en CursoEstudiante
        Estudiante est = crearEstudiante("Laura", "Gómez", "EST-LAU-" + UUID.randomUUID());
        cursoEstudianteRepository.save(new CursoEstudiante(curso, est, "2026"));

        // Primer registro: PRESENTE
        asistenciaService.registrar(sesionId, List.of(new AsistenciaItemDto(est.getId(), "PRESENTE")));
        List<Asistencia> asistencias1 = asistenciaRepository.findBySesionId(sesionId);
        assertEquals(1, asistencias1.size());
        assertEquals("PRESENTE", asistencias1.get(0).getEstado());

        // Segundo registro: cambio a AUSENTE con observaciones de retardo/excusa
        asistenciaService.registrar(sesionId, List.of(new AsistenciaItemDto(est.getId(), "AUSENTE", "Llegó con retraso justificable")));
        List<Asistencia> asistencias2 = asistenciaRepository.findBySesionId(sesionId);
        assertEquals(1, asistencias2.size(), "Upsert no debe crear una segunda fila de asistencia");
        assertEquals("AUSENTE", asistencias2.get(0).getEstado());
        assertEquals("Llegó con retraso justificable", asistencias2.get(0).getObservaciones());
    }

    @Test
    @Transactional
    @DisplayName("Prueba 6: Sesión CANCELADA rechaza cualquier registro de asistencia")
    void testSesionCanceladaRechazaRegistro() {
        Curso curso = crearCurso("7°", "02", "Mañana", "2026");
        Materia mat = crearMateria("Ética 7", "ETI-7-" + UUID.randomUUID());
        Docente doc = crearDocente("Profe", "Ética", "DOC-ETI-" + UUID.randomUUID());
        cursoMateriaRepository.save(new CursoMateria(curso, mat, doc, "2026"));

        Horario h = new Horario();
        h.setCurso(curso);
        h.setMateria(mat);
        h.setDiaSemana("Lunes");
        h.setHoraInicio(LocalTime.of(10, 30));
        h.setHoraFin(LocalTime.of(12, 0));
        horarioRepository.save(h);

        LocalDate lunes = LocalDate.of(2026, 3, 2);
        List<SesionClase> sesiones = asistenciaService.abrirDia(curso.getId(), lunes);
        UUID sesionId = sesiones.get(0).getId();

        Estudiante est = crearEstudiante("Estudiante", "Uno", "EST-001-" + UUID.randomUUID());
        cursoEstudianteRepository.save(new CursoEstudiante(curso, est, "2026"));

        // Cancelar sesión
        asistenciaService.cancelarSesion(sesionId, "Actividad institucional de izada de bandera");
        SesionClase cancelada = sesionClaseRepository.findById(sesionId).orElseThrow();
        assertEquals("CANCELADA", cancelada.getEstado());

        // Intentar registrar asistencia debe fallar
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                asistenciaService.registrar(sesionId, List.of(new AsistenciaItemDto(est.getId(), "PRESENTE"))));

        assertTrue(ex.getMessage().contains("sesión cancelada"));
    }

    @Test
    @Transactional
    @DisplayName("Prueba 7: Asignar REEMPLAZO actualiza el docente efectivo y permite registrar con el nuevo docente")
    void testAsignarReemplazoDocenteEfectivoYEstadoReemplazo() {
        Curso curso = crearCurso("8°", "02", "Mañana", "2026");
        Materia mat = crearMateria("Química 8", "QUI-8-" + UUID.randomUUID());
        Docente titular = crearDocente("Titular", "Química", "DOC-TIT-" + UUID.randomUUID());
        Docente sustituto = crearDocente("Sustituto", "Suplente", "DOC-SUS-" + UUID.randomUUID());

        cursoMateriaRepository.save(new CursoMateria(curso, mat, titular, "2026"));

        LocalDate hoy = LocalDate.now(clock);
        String diaSemana = com.siga.siga_iea.clases.application.CursoGestionAppService.obtenerNombreDiaEspanol(hoy.getDayOfWeek());

        Horario h = new Horario();
        h.setCurso(curso);
        h.setMateria(mat);
        h.setDiaSemana(diaSemana);
        h.setHoraInicio(LocalTime.of(7, 0));
        h.setHoraFin(LocalTime.of(8, 30));
        horarioRepository.save(h);

        List<SesionClase> sesiones = asistenciaService.abrirDia(curso.getId(), hoy);
        UUID sesionId = sesiones.get(0).getId();

        Estudiante est = crearEstudiante("Estudiante", "Dos", "EST-002-" + UUID.randomUUID());
        cursoEstudianteRepository.save(new CursoEstudiante(curso, est, "2026"));

        // Asignar docente de reemplazo
        asistenciaService.asignarReemplazo(sesionId, sustituto.getId(), "Incapacidad médica del titular");

        SesionClase sesionReemplazo = sesionClaseRepository.findById(sesionId).orElseThrow();
        assertEquals("REEMPLAZO", sesionReemplazo.getEstado());
        assertEquals(sustituto.getId(), sesionReemplazo.getDocente().getId());

        // El docente de reemplazo registra asistencia exitosamente
        List<Asistencia> asistencias = asistenciaService.registrar(
                sesionId,
                List.of(new AsistenciaItemDto(est.getId(), "PRESENTE")),
                sustituto,
                RolEnum.DOCENTE
        );
        assertEquals(1, asistencias.size());
    }

    @Autowired
    private java.time.Clock clock;

    @Test
    @Transactional
    @DisplayName("Prueba 8: Autorización estricta: docente ajeno es rechazado, docente titular o admin son permitidos")
    void testAutorizacionSoloDocenteAsignadoOAdminPuedeRegistrar() {
        Curso curso = crearCurso("9°", "02", "Mañana", "2026");
        Materia mat = crearMateria("Historia 9", "HIS-9-" + UUID.randomUUID());
        Docente titular = crearDocente("Titular", "Historia", "DOC-HIST-" + UUID.randomUUID());
        Docente docenteAjeno = crearDocente("Ajeno", "EducaciónFísica", "DOC-AJENO-" + UUID.randomUUID());

        cursoMateriaRepository.save(new CursoMateria(curso, mat, titular, "2026"));

        LocalDate hoy = LocalDate.now(clock);
        String diaSemana = com.siga.siga_iea.clases.application.CursoGestionAppService.obtenerNombreDiaEspanol(hoy.getDayOfWeek());

        Horario h = new Horario();
        h.setCurso(curso);
        h.setMateria(mat);
        h.setDiaSemana(diaSemana);
        h.setHoraInicio(LocalTime.of(7, 0));
        h.setHoraFin(LocalTime.of(8, 30));
        horarioRepository.save(h);

        List<SesionClase> sesiones = asistenciaService.abrirDia(curso.getId(), hoy);
        UUID sesionId = sesiones.get(0).getId();

        Estudiante est = crearEstudiante("Estudiante", "Tres", "EST-003-" + UUID.randomUUID());
        cursoEstudianteRepository.save(new CursoEstudiante(curso, est, "2026"));

        // 1. Docente ajeno intenta registrar -> SecurityException
        assertThrows(SecurityException.class, () ->
                asistenciaService.registrar(
                        sesionId,
                        List.of(new AsistenciaItemDto(est.getId(), "PRESENTE")),
                        docenteAjeno,
                        RolEnum.DOCENTE
                ));

        // 2. Docente titular intenta registrar -> Permitido
        List<Asistencia> guardadasDocente = asistenciaService.registrar(
                sesionId,
                List.of(new AsistenciaItemDto(est.getId(), "PRESENTE")),
                titular,
                RolEnum.DOCENTE
        );
        assertEquals(1, guardadasDocente.size());

        // 3. Coordinador/Admin intenta registrar -> Permitido
        List<Asistencia> guardadasAdmin = asistenciaService.registrar(
                sesionId,
                List.of(new AsistenciaItemDto(est.getId(), "AUSENTE", "Justificado en rectoría")),
                null,
                RolEnum.ADMIN
        );
        assertEquals(1, guardadasAdmin.size());
        assertEquals("AUSENTE", guardadasAdmin.get(0).getEstado());
    }

    @Test
    @Transactional
    @DisplayName("Prueba 10: Ventana de edición: docente no puede registrar en una sesión de fecha distinta a hoy")
    void testVentanaEdicionDocenteRechazadoEnFechaDistintaAHoy() {
        Curso curso = crearCurso("10°", "01", "Mañana", "2026");
        Materia mat = crearMateria("Química 10", "QUI-10-" + UUID.randomUUID());
        Docente titular = crearDocente("Docente", "Química", "DOC-QUI-" + UUID.randomUUID());
        cursoMateriaRepository.save(new CursoMateria(curso, mat, titular, "2026"));

        // Fecha de ayer (fuera de la ventana del docente)
        LocalDate ayer = LocalDate.now(clock).minusDays(1);
        String diaAyer = com.siga.siga_iea.clases.application.CursoGestionAppService.obtenerNombreDiaEspanol(ayer.getDayOfWeek());

        Horario h = new Horario();
        h.setCurso(curso);
        h.setMateria(mat);
        h.setDiaSemana(diaAyer);
        h.setHoraInicio(LocalTime.of(8, 0));
        h.setHoraFin(LocalTime.of(9, 30));
        horarioRepository.save(h);

        List<SesionClase> sesiones = asistenciaService.abrirDia(curso.getId(), ayer);
        UUID sesionId = sesiones.get(0).getId();

        Estudiante est = crearEstudiante("Est", "Ayer", "EST-AYER-" + UUID.randomUUID());
        cursoEstudianteRepository.save(new CursoEstudiante(curso, est, "2026"));

        // Docente intenta registrar en fecha pasada -> SecurityException
        SecurityException ex = assertThrows(SecurityException.class, () ->
                asistenciaService.registrar(
                        sesionId,
                        List.of(new AsistenciaItemDto(est.getId(), "PRESENTE")),
                        titular,
                        RolEnum.DOCENTE
                ));
        assertTrue(ex.getMessage().contains("Ventana de edición cerrada"));

        // Admin sí puede registrar fuera de la ventana de edición
        List<Asistencia> asistenciasAdmin = asistenciaService.registrar(
                sesionId,
                List.of(new AsistenciaItemDto(est.getId(), "EXCUSADO", "Autorizado por coordinación")),
                null,
                RolEnum.ADMIN
        );
        assertEquals(1, asistenciasAdmin.size());
        assertEquals("EXCUSADO", asistenciasAdmin.get(0).getEstado());
    }

    @Test
    @DisplayName("Prueba 9: Concurrencia en abrirDia: aperturas simultáneas no generan duplicados ni fallan")
    void testConcurrenciaAbrirDiaSimultaneoNoFallaNiDuplica() throws Exception {
        Curso curso = crearCurso("6°", "01", "Mañana", "2026");
        Materia mat = crearMateria("Tecnología 6", "TEC-6-" + UUID.randomUUID());
        Docente doc = crearDocente("Profe", "Tecnología", "DOC-TEC-" + UUID.randomUUID());

        cursoMateriaRepository.save(new CursoMateria(curso, mat, doc, "2026"));

        Horario h = new Horario();
        h.setCurso(curso);
        h.setMateria(mat);
        h.setDiaSemana("Jueves");
        h.setHoraInicio(LocalTime.of(8, 30));
        h.setHoraFin(LocalTime.of(10, 0));
        horarioRepository.save(h);

        LocalDate jueves = LocalDate.of(2026, 3, 5);

        int numHilos = 4;
        ExecutorService executor = Executors.newFixedThreadPool(numHilos);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<List<SesionClase>>> futures = new ArrayList<>();

        for (int i = 0; i < numHilos; i++) {
            futures.add(executor.submit(() -> {
                startLatch.await(); // Sincronizar todos los hilos para que inicien en el mismo instante
                return asistenciaService.abrirDia(curso.getId(), jueves);
            }));
        }

        startLatch.countDown(); // Despertar a los hilos concurrentes

        for (Future<List<SesionClase>> future : futures) {
            List<SesionClase> res = future.get(5, TimeUnit.SECONDS);
            assertEquals(1, res.size(), "Cada hilo debe obtener exactamente 1 sesión");
        }

        executor.shutdown();

        // En BD debe existir exactamente 1 sesión sin duplicados
        List<SesionClase> enBd = sesionClaseRepository.findByCursoIdAndFecha(curso.getId(), jueves);
        assertEquals(1, enBd.size(), "La restricción única y el manejo de concurrencia deben asegurar exactamente 1 fila");
    }

    @Test
    @Transactional
    @DisplayName("Prueba 10: Crear sesión extraordinaria valida choques y límites de jornada usando HorarioValidator")
    void testSesionExtraordinariaValidaConHorarioValidator() {
        Curso curso = crearCurso("7°", "01", "Mañana", "2026");
        Materia mat = crearMateria("Sociales 7", "SOC-7-" + UUID.randomUUID());
        Docente doc = crearDocente("Profesor", "Sociales", "DOC-SOC-" + UUID.randomUUID());
        cursoMateriaRepository.save(new CursoMateria(curso, mat, doc, "2026"));

        LocalDate fecha = LocalDate.of(2026, 3, 2);

        // Intento de sesión extraordinaria que termina a las 14:00 (fuera de jornada Mañana que termina a las 12:00)
        assertThrows(IllegalArgumentException.class, () ->
                asistenciaService.crearSesionExtraordinaria(
                        curso.getId(), mat.getId(), doc.getId(),
                        fecha, LocalTime.of(11, 0), LocalTime.of(14, 0),
                        "Taller de refuerzo fuera de jornada"
                ));

        // Sesión extraordinaria válida dentro de jornada
        SesionClase extra = asistenciaService.crearSesionExtraordinaria(
                curso.getId(), mat.getId(), doc.getId(),
                fecha, LocalTime.of(10, 0), LocalTime.of(11, 30),
                "Clase de recuperación"
        );

        assertNotNull(extra.getId());
        assertEquals("Clase de recuperación", extra.getTema());
    }

    @Test
    @Transactional
    @DisplayName("Prueba 11: Sesión extraordinaria detecta solapamiento parcial/total con SesionClase existentes en el día")
    void testSesionExtraordinariaDetectaChoqueConSesionClaseDelDia() {
        Curso cursoA = crearCurso("8°", "01", "Mañana", "2026");
        Curso cursoB = crearCurso("8°", "02", "Mañana", "2026");
        Materia mat = crearMateria("Inglés 8", "ENG-8-" + UUID.randomUUID());
        Docente doc = crearDocente("Teacher", "Smith", "DOC-ENG-" + UUID.randomUUID());

        LocalDate fecha = LocalDate.of(2026, 3, 3); // Martes

        // 1. Crear primera sesión extraordinaria para cursoA (08:00 - 09:30)
        SesionClase primera = asistenciaService.crearSesionExtraordinaria(
                cursoA.getId(), mat.getId(), doc.getId(),
                fecha, LocalTime.of(8, 0), LocalTime.of(9, 30),
                "Refuerzo A"
        );
        assertNotNull(primera.getId());

        // 2. Intentar crear sesión en cursoA con solapamiento parcial (09:00 - 10:30) -> Debe fallar por choque de curso
        IllegalStateException exCurso = assertThrows(IllegalStateException.class, () ->
                asistenciaService.crearSesionExtraordinaria(
                        cursoA.getId(), mat.getId(), null,
                        fecha, LocalTime.of(9, 0), LocalTime.of(10, 30),
                        "Solapamiento curso"
                ));
        assertTrue(exCurso.getMessage().contains("Choque de horario: El curso ya tiene una sesión programada"));

        // 3. Intentar crear sesión en cursoB con el mismo docente (08:30 - 10:00) -> Debe fallar por choque de docente
        IllegalStateException exDocente = assertThrows(IllegalStateException.class, () ->
                asistenciaService.crearSesionExtraordinaria(
                        cursoB.getId(), mat.getId(), doc.getId(),
                        fecha, LocalTime.of(8, 30), LocalTime.of(10, 0),
                        "Solapamiento docente"
                ));
        assertTrue(exDocente.getMessage().contains("Choque de horario: El docente"));

        // 4. Si la sesión anterior es cancelada, ya no debe bloquear la nueva sesión
        asistenciaService.cancelarSesion(primera.getId(), "Profesor no pudo asistir");
        SesionClase nueva = asistenciaService.crearSesionExtraordinaria(
                cursoA.getId(), mat.getId(), doc.getId(),
                fecha, LocalTime.of(8, 0), LocalTime.of(9, 30),
                "Nueva sesión tras cancelación"
        );
        assertNotNull(nueva.getId());
        assertEquals("DICTADA", nueva.getEstado());
    }
}
