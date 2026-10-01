package com.siga.siga_iea.asistencias;

import com.siga.siga_iea.asistencias.dto.AsistenciaItemDto;
import com.siga.siga_iea.asistencias.entity.Asistencia;
import com.siga.siga_iea.asistencias.entity.SesionClase;
import com.siga.siga_iea.asistencias.repository.AsistenciaRepository;
import com.siga.siga_iea.asistencias.repository.SesionClaseRepository;
import com.siga.siga_iea.asistencias.service.AsistenciaService;
import com.siga.siga_iea.asistencias.service.MiJornadaService;
import com.siga.siga_iea.auth.enums.RolEnum;
import com.siga.siga_iea.clases.entity.*;
import com.siga.siga_iea.clases.repository.*;
import com.siga.siga_iea.usuarios.entity.Docente;
import com.siga.siga_iea.usuarios.entity.Estudiante;
import com.siga.siga_iea.usuarios.entity.Usuario;
import com.siga.siga_iea.usuarios.repository.DocenteRepository;
import com.siga.siga_iea.usuarios.repository.EstudianteRepository;
import com.siga.siga_iea.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(MiJornadaControllerTest.FixedClockConfig.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testjornada;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "spring.main.allow-bean-definition-overriding=true"
})
class MiJornadaControllerTest {

    public static class MutableTestClock extends Clock {
        private Instant instant = Instant.parse("2026-09-30T15:00:00Z"); // Miércoles
        private final ZoneId zone = ZoneId.of("America/Bogota");

        public void setInstant(Instant instant) {
            this.instant = instant;
        }

        public void reset() {
            this.instant = Instant.parse("2026-09-30T15:00:00Z");
        }

        @Override public ZoneId getZone() { return zone; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return instant; }
    }

    @TestConfiguration
    public static class FixedClockConfig {
        @Bean
        @Primary
        public Clock clock() {
            return new MutableTestClock();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Clock clock;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private MateriaRepository materiaRepository;

    @Autowired
    private DocenteRepository docenteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CursoMateriaRepository cursoMateriaRepository;

    @Autowired
    private HorarioRepository horarioRepository;

    @Autowired
    private SesionClaseRepository sesionClaseRepository;

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @Autowired
    private CursoEstudianteRepository cursoEstudianteRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private AsistenciaService asistenciaService;

    @BeforeEach
    void setUp() {
        if (clock instanceof MutableTestClock mutableClock) {
            mutableClock.reset();
        }
    }

    @AfterEach
    void tearDown() {
        if (clock instanceof MutableTestClock mutableClock) {
            mutableClock.reset();
        }
    }

    private Docente crearDocente(String nombres, String apellidos, String doc) {
        Docente d = new Docente();
        d.setNombres(nombres);
        d.setApellidos(apellidos);
        d.setNumeroDocumento(doc);
        d.setEstado("Activo");
        return docenteRepository.save(d);
    }

    private Usuario crearUsuario(String email, String doc, RolEnum rol) {
        Usuario u = new Usuario();
        u.setEmail(email);
        u.setPassword("password123");
        u.setRol(rol.name());
        u.setNumeroDocumento(doc);
        u.setEstado("Activo");
        return usuarioRepository.save(u);
    }

    private Curso crearCurso(String grado, String grupo, Docente director) {
        Curso c = new Curso();
        c.setGrado(grado);
        c.setGrupo(grupo);
        c.setJornada("Mañana");
        c.setAnoLectivo("2026");
        c.setDirector(director);
        return cursoRepository.save(c);
    }

    private Materia crearMateria(String nombre) {
        Materia m = new Materia(nombre, "MAT-" + UUID.randomUUID());
        return materiaRepository.save(m);
    }

    private Estudiante crearEstudiante(String nombres, String apellidos, String doc) {
        Estudiante e = new Estudiante();
        e.setNombres(nombres);
        e.setApellidos(apellidos);
        e.setNumeroDocumento(doc);
        e.setEstado("Activo");
        return estudianteRepository.save(e);
    }

    // =========================================================================
    // PRUEBA 1: Docente A pide GET de sesión de B -> 403. POST -> sin cambios en BD
    // =========================================================================
    @Test
    @Transactional
    @WithMockUser(username = "docenteA@ieaci.edu.co", roles = "DOCENTE")
    @DisplayName("1. Docente A pide GET /sesiones/{id} de B -> 403. POST guardar -> sin cambios en BD")
    void testDocenteANoPuedeVerNiGuardarSesionDeDocenteB() throws Exception {
        Docente docA = crearDocente("Docente", "Alfa", "DOC-ALFA-01");
        crearUsuario("docenteA@ieaci.edu.co", "DOC-ALFA-01", RolEnum.DOCENTE);

        Docente docB = crearDocente("Docente", "Beta", "DOC-BETA-02");
        crearUsuario("docenteB@ieaci.edu.co", "DOC-BETA-02", RolEnum.DOCENTE);

        Curso cursoB = crearCurso("10°", "01", docB);
        Materia matB = crearMateria("Filosofía 10");
        cursoMateriaRepository.save(new CursoMateria(cursoB, matB, docB, "2026"));

        LocalDate hoy = LocalDate.now(clock);
        SesionClase sesionB = new SesionClase(
                cursoB, null, null, docB, hoy, LocalTime.of(7, 0), LocalTime.of(8, 30), "ASIGNATURA"
        );
        sesionB.setEstado("DICTADA");
        sesionB = sesionClaseRepository.save(sesionB);

        Estudiante est = crearEstudiante("Estudiante", "Uno", "EST-001");
        cursoEstudianteRepository.save(new CursoEstudiante(cursoB, est, "2026"));

        // 1. GET sesión ajena -> 403 Forbidden
        mockMvc.perform(get("/mi-jornada/sesiones/" + sesionB.getId()))
                .andExpect(status().isForbidden());

        // 2. POST guardar sesión ajena -> rechaza y BD permanece intacta (0 asistencias)
        mockMvc.perform(post("/mi-jornada/sesiones/" + sesionB.getId() + "/guardar")
                        .with(csrf())
                        .param("estudianteId", est.getId().toString())
                        .param("estado", "PRESENTE"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No tienes permiso sobre esta sesión")));

        List<Asistencia> asistenciasEnBD = asistenciaRepository.findBySesionId(sesionB.getId());
        assertEquals(0, asistenciasEnBD.size(), "La base de datos no debe ser modificada por un docente ajeno");
    }

    // =========================================================================
    // PRUEBA 2: Docente sin horarios hoy -> la vista tiene tieneClases=false
    // =========================================================================
    @Test
    @Transactional
    @WithMockUser(username = "docenteSinHorarios@ieaci.edu.co", roles = "DOCENTE")
    @DisplayName("2. Docente sin horarios hoy -> la vista tiene tieneClases=false")
    void testDocenteSinHorariosHoyTieneClasesFalse() throws Exception {
        Docente doc = crearDocente("Docente", "Libre", "DOC-LIBRE-03");
        crearUsuario("docenteSinHorarios@ieaci.edu.co", "DOC-LIBRE-03", RolEnum.DOCENTE);

        // Se le asigna un horario pero en JUEVES (hoy es Miércoles 2026-09-30)
        Curso curso = crearCurso("7°", "01", null);
        Materia mat = crearMateria("Artes 7");
        cursoMateriaRepository.save(new CursoMateria(curso, mat, doc, "2026"));

        Horario h = new Horario();
        h.setCurso(curso);
        h.setMateria(mat);
        h.setDiaSemana("Jueves");
        h.setHoraInicio(LocalTime.of(9, 0));
        h.setHoraFin(LocalTime.of(10, 30));
        horarioRepository.save(h);

        mockMvc.perform(get("/mi-jornada"))
                .andExpect(status().isOk())
                .andExpect(view().name("mi-jornada/index"))
                .andExpect(model().attribute("vista", hasProperty("tieneClases", is(false))))
                .andExpect(model().attribute("vista", hasProperty("diaHabil", is(true))));
    }

    // =========================================================================
    // PRUEBA 3: POST /mi-jornada/iniciar dos veces -> count(sesiones) igual (idempotente)
    // =========================================================================
    @Test
    @Transactional
    @WithMockUser(username = "docenteIdempotente@ieaci.edu.co", roles = "DOCENTE")
    @DisplayName("3. POST /mi-jornada/iniciar dos veces -> count(sesiones) igual (idempotente)")
    void testIniciarJornadaEsIdempotente() throws Exception {
        Docente doc = crearDocente("Docente", "Idempotente", "DOC-IDEM-04");
        crearUsuario("docenteIdempotente@ieaci.edu.co", "DOC-IDEM-04", RolEnum.DOCENTE);

        Curso curso = crearCurso("8°", "02", null);
        Materia mat = crearMateria("Ciencias 8");
        cursoMateriaRepository.save(new CursoMateria(curso, mat, doc, "2026"));

        Horario h = new Horario();
        h.setCurso(curso);
        h.setMateria(mat);
        h.setDiaSemana("Miércoles"); // coincide con la fecha fija del clock
        h.setHoraInicio(LocalTime.of(7, 0));
        h.setHoraFin(LocalTime.of(8, 30));
        horarioRepository.save(h);

        LocalDate hoy = LocalDate.now(clock);

        // Primer POST
        mockMvc.perform(post("/mi-jornada/iniciar").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("mi-jornada/fragments :: sesiones"));

        long totalPrimeraVez = sesionClaseRepository.findByDocenteIdAndFecha(doc.getId(), hoy).size();
        assertEquals(1, totalPrimeraVez, "Debe crearse exactamente 1 sesión en la primera apertura");

        // Segundo POST (idempotente)
        mockMvc.perform(post("/mi-jornada/iniciar").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("mi-jornada/fragments :: sesiones"));

        long totalSegundaVez = sesionClaseRepository.findByDocenteIdAndFecha(doc.getId(), hoy).size();
        assertEquals(totalPrimeraVez, totalSegundaVez, "La segunda apertura no debe crear duplicados");
    }

    // =========================================================================
    // PRUEBA 4: Sesión CANCELADA -> la vista deshabilita controles y guardar devuelve mensaje de error
    // =========================================================================
    @Test
    @Transactional
    @WithMockUser(username = "docenteCancelada@ieaci.edu.co", roles = "DOCENTE")
    @DisplayName("4. Sesión CANCELADA -> vista muestra advertencia y guardar devuelve mensaje de error")
    void testSesionCanceladaDeshabilitaYRechazaGuardado() throws Exception {
        Docente doc = crearDocente("Docente", "Cancelada", "DOC-CANC-05");
        crearUsuario("docenteCancelada@ieaci.edu.co", "DOC-CANC-05", RolEnum.DOCENTE);

        Curso curso = crearCurso("9°", "01", doc);
        LocalDate hoy = LocalDate.now(clock);

        SesionClase sesion = new SesionClase(
                curso, null, null, doc, hoy, LocalTime.of(7, 0), LocalTime.of(12, 0), "JORNADA"
        );
        sesion.setEstado("CANCELADA");
        sesion = sesionClaseRepository.save(sesion);

        Estudiante est = crearEstudiante("Estudiante", "Cancelado", "EST-CANC-01");
        cursoEstudianteRepository.save(new CursoEstudiante(curso, est, "2026"));

        // 1. GET sesión cancelada: renderiza fragmento con aviso de cancelada
        mockMvc.perform(get("/mi-jornada/sesiones/" + sesion.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sesión cancelada — no se puede registrar asistencia")));

        // 2. POST guardar en sesión cancelada: devuelve mensaje de error
        mockMvc.perform(post("/mi-jornada/sesiones/" + sesion.getId() + "/guardar")
                        .with(csrf())
                        .param("estudianteId", est.getId().toString())
                        .param("estado", "PRESENTE"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No se puede registrar asistencia en una sesión cancelada")));

        assertEquals(0, asistenciaRepository.findBySesionId(sesion.getId()).size(), "No debe persistirse asistencia en sesión cancelada");
    }

    // =========================================================================
    // PRUEBA 5: PERSONAL_ADMINISTRATIVO ve y guarda cualquier sesión
    // =========================================================================
    @Test
    @Transactional
    @WithMockUser(username = "admin_personal@ieaci.edu.co", roles = "PERSONAL_ADMINISTRATIVO")
    @DisplayName("5. PERSONAL_ADMINISTRATIVO ve y guarda cualquier sesión")
    void testPersonalAdministrativoVeYGuardaCualquierSesion() throws Exception {
        Docente titular = crearDocente("Titular", "Clase", "DOC-TIT-06");
        crearUsuario("admin_personal@ieaci.edu.co", "ADM-PERS-001", RolEnum.PERSONAL_ADMINISTRATIVO);

        Curso curso = crearCurso("11°", "02", titular);
        LocalDate hoy = LocalDate.now(clock);

        SesionClase sesion = new SesionClase(
                curso, null, null, titular, hoy, LocalTime.of(8, 0), LocalTime.of(9, 30), "ASIGNATURA"
        );
        sesion.setEstado("DICTADA");
        sesion = sesionClaseRepository.save(sesion);

        Estudiante est = crearEstudiante("Estudiante", "Admin", "EST-ADM-01");
        cursoEstudianteRepository.save(new CursoEstudiante(curso, est, "2026"));

        // 1. Admin consulta sesión del docente titular -> 200 OK
        mockMvc.perform(get("/mi-jornada/sesiones/" + sesion.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("mi-jornada/fragments :: tabla"));

        // 2. Admin registra asistencia en sesión del docente titular -> éxito y guardado
        mockMvc.perform(post("/mi-jornada/sesiones/" + sesion.getId() + "/guardar")
                        .with(csrf())
                        .param("estudianteId", est.getId().toString())
                        .param("estado", "PRESENTE"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Asistencia registrada exitosamente")));

        List<Asistencia> enBD = asistenciaRepository.findBySesionId(sesion.getId());
        assertEquals(1, enBD.size(), "El personal administrativo debe poder guardar asistencia");
        assertEquals("PRESENTE", enBD.get(0).getEstado());
    }

    // =========================================================================
    // PRUEBA 6: Con Clock en sábado -> diaHabil=false y el botón no aparece
    // =========================================================================
    @Test
    @Transactional
    @WithMockUser(username = "docenteSabado@ieaci.edu.co", roles = "DOCENTE")
    @DisplayName("6. Con Clock en sábado -> diaHabil=false y el botón no aparece")
    void testClockEnSabadoDiaHabilFalseYNoApareceBoton() throws Exception {
        Docente doc = crearDocente("Docente", "FinSemana", "DOC-SAB-07");
        crearUsuario("docenteSabado@ieaci.edu.co", "DOC-SAB-07", RolEnum.DOCENTE);

        // Simulamos que hoy es sábado 3 de octubre de 2026
        if (clock instanceof MutableTestClock mutableClock) {
            mutableClock.setInstant(Instant.parse("2026-10-03T15:00:00Z"));
        }

        mockMvc.perform(get("/mi-jornada"))
                .andExpect(status().isOk())
                .andExpect(view().name("mi-jornada/index"))
                .andExpect(model().attribute("vista", hasProperty("diaHabil", is(false))))
                .andExpect(content().string(containsString("Fin de semana")))
                .andExpect(content().string(not(containsString("btn-iniciar-jornada"))));
    }

    // =========================================================================
    // PRUEBA 7: Docente de 3° ve una sesión JORNADA, y uno de bachillerato ve varias
    // =========================================================================
    @Test
    @Transactional
    @WithMockUser(username = "docentePrimaria@ieaci.edu.co", roles = "DOCENTE")
    @DisplayName("7. Docente de 3° (primaria) ve una sesión JORNADA")
    void testDocentePrimariaVeUnaSesionJornada() throws Exception {
        Docente docPrimaria = crearDocente("Docente", "Tercero", "DOC-PRIM-08");
        crearUsuario("docentePrimaria@ieaci.edu.co", "DOC-PRIM-08", RolEnum.DOCENTE);

        // Docente de primaria es director de 3° - 01
        Curso curso3 = crearCurso("3°", "01", docPrimaria);

        mockMvc.perform(post("/mi-jornada/iniciar").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("vista", hasProperty("sesiones", hasSize(1))));

        LocalDate hoy = LocalDate.now(clock);
        List<SesionClase> sesiones = sesionClaseRepository.findByDocenteIdAndFecha(docPrimaria.getId(), hoy);
        assertEquals(1, sesiones.size());
        assertEquals("JORNADA", sesiones.get(0).getTipo());
    }

    @Test
    @Transactional
    @WithMockUser(username = "docenteBachillerato@ieaci.edu.co", roles = "DOCENTE")
    @DisplayName("7b. Docente de bachillerato con múltiples bloques ve varias sesiones ASIGNATURA")
    void testDocenteBachilleratoVeVariasSesiones() throws Exception {
        Docente docBach = crearDocente("Docente", "Secundaria", "DOC-BACH-09");
        crearUsuario("docenteBachillerato@ieaci.edu.co", "DOC-BACH-09", RolEnum.DOCENTE);

        Curso curso8 = crearCurso("8°", "01", null);
        Materia matBio = crearMateria("Biología 8");
        cursoMateriaRepository.save(new CursoMateria(curso8, matBio, docBach, "2026"));

        // Bloque 1: 07:00 a 08:30
        Horario h1 = new Horario();
        h1.setCurso(curso8);
        h1.setMateria(matBio);
        h1.setDiaSemana("Miércoles");
        h1.setHoraInicio(LocalTime.of(7, 0));
        h1.setHoraFin(LocalTime.of(8, 30));
        horarioRepository.save(h1);

        // Bloque 2: 08:30 a 10:00
        Horario h2 = new Horario();
        h2.setCurso(curso8);
        h2.setMateria(matBio);
        h2.setDiaSemana("Miércoles");
        h2.setHoraInicio(LocalTime.of(8, 30));
        h2.setHoraFin(LocalTime.of(10, 0));
        horarioRepository.save(h2);

        mockMvc.perform(post("/mi-jornada/iniciar").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("vista", hasProperty("sesiones", hasSize(2))));

        LocalDate hoy = LocalDate.now(clock);
        List<SesionClase> sesiones = sesionClaseRepository.findByDocenteIdAndFecha(docBach.getId(), hoy);
        assertEquals(2, sesiones.size());
        assertTrue(sesiones.stream().allMatch(s -> "ASIGNATURA".equals(s.getTipo())));
    }
}
