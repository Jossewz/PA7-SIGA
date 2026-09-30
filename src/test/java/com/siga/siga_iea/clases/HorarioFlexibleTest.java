package com.siga.siga_iea.clases;

import com.siga.siga_iea.clases.dto.HorarioDto;
import com.siga.siga_iea.clases.entity.Curso;
import com.siga.siga_iea.clases.entity.CursoMateria;
import com.siga.siga_iea.clases.entity.Horario;
import com.siga.siga_iea.clases.entity.Materia;
import com.siga.siga_iea.clases.repository.CursoMateriaRepository;
import com.siga.siga_iea.clases.repository.CursoRepository;
import com.siga.siga_iea.clases.repository.HorarioRepository;
import com.siga.siga_iea.clases.repository.MateriaRepository;
import com.siga.siga_iea.clases.service.CursoService;
import com.siga.siga_iea.clases.validation.HorarioValidator;
import com.siga.siga_iea.usuarios.entity.Docente;
import com.siga.siga_iea.usuarios.repository.DocenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class HorarioFlexibleTest {

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private MateriaRepository materiaRepository;

    @Autowired
    private DocenteRepository docenteRepository;

    @Autowired
    private HorarioRepository horarioRepository;

    @Autowired
    private CursoMateriaRepository cursoMateriaRepository;

    @Autowired
    private HorarioValidator horarioValidator;

    @Autowired
    private CursoService cursoService;

    private Curso cursoManana;
    private Curso cursoTarde;
    private Materia matematicas;
    private Materia fisica;
    private Docente docenteCarlos;
    private Docente docenteAna;

    @BeforeEach
    void setUp() {
        cursoManana = new Curso();
        cursoManana.setGrado("10°");
        cursoManana.setGrupo("01");
        cursoManana.setJornada("Mañana");
        cursoManana.setAnoLectivo("2026");
        cursoManana = cursoRepository.save(cursoManana);

        cursoTarde = new Curso();
        cursoTarde.setGrado("10°");
        cursoTarde.setGrupo("02");
        cursoTarde.setJornada("Tarde");
        cursoTarde.setAnoLectivo("2026");
        cursoTarde = cursoRepository.save(cursoTarde);

        matematicas = new Materia("Matemáticas", "MAT-10");
        matematicas.setIntensidadHoraria(4);
        matematicas = materiaRepository.save(matematicas);

        fisica = new Materia("Física Elemental", "FIS-10");
        fisica.setIntensidadHoraria(2);
        fisica = materiaRepository.save(fisica);

        docenteCarlos = new Docente();
        docenteCarlos.setNombres("Carlos");
        docenteCarlos.setApellidos("Gómez");
        docenteCarlos.setNumeroDocumento("10203040");
        docenteCarlos = docenteRepository.save(docenteCarlos);

        docenteAna = new Docente();
        docenteAna.setNombres("Ana");
        docenteAna.setApellidos("Ríos");
        docenteAna.setNumeroDocumento("50607080");
        docenteAna = docenteRepository.save(docenteAna);

        // Asignar docenteCarlos a Matemáticas en cursoManana vía CursoMateria
        CursoMateria cm = new CursoMateria(cursoManana, matematicas, docenteCarlos, "2026");
        cursoMateriaRepository.save(cm);
    }

    @Test
    @DisplayName("1. hora_fin <= hora_inicio se rechaza (incluye igualdad y orden inverso)")
    void testHoraFinMenorOIgualSeRechaza() {
        // Igualdad 08:00 - 08:00
        HorarioDto dtoIgual = new HorarioDto(cursoManana.getId(), "Lunes", LocalTime.of(8, 0), LocalTime.of(8, 0),
                matematicas.getId(), docenteCarlos.getId(), null, "Aula 101");
        IllegalArgumentException exIgual = assertThrows(IllegalArgumentException.class, () ->
                horarioValidator.validar(dtoIgual, null));
        assertTrue(exIgual.getMessage().contains("estrictamente posterior"));

        // Inverso 09:00 - 08:00
        HorarioDto dtoInverso = new HorarioDto(cursoManana.getId(), "Lunes", LocalTime.of(9, 0), LocalTime.of(8, 0),
                matematicas.getId(), docenteCarlos.getId(), null, "Aula 101");
        IllegalArgumentException exInv = assertThrows(IllegalArgumentException.class, () ->
                horarioValidator.validar(dtoInverso, null));
        assertTrue(exInv.getMessage().contains("estrictamente posterior"));
    }

    @Test
    @DisplayName("2. Franja fuera de límites de jornada se rechaza (Mañana 07:00-12:00, Tarde 13:00-18:00)")
    void testFranjaFueraDeLimitesJornadaSeRechaza() {
        // Mañana empieza antes de 07:00
        HorarioDto mananaAntes = new HorarioDto(cursoManana.getId(), "Lunes", LocalTime.of(6, 45), LocalTime.of(8, 15),
                matematicas.getId(), docenteCarlos.getId(), null, "Aula 101");
        assertThrows(IllegalArgumentException.class, () -> horarioValidator.validar(mananaAntes, null));

        // Mañana termina después de 12:00
        HorarioDto mananaDespues = new HorarioDto(cursoManana.getId(), "Lunes", LocalTime.of(11, 0), LocalTime.of(12, 15),
                matematicas.getId(), docenteCarlos.getId(), null, "Aula 101");
        assertThrows(IllegalArgumentException.class, () -> horarioValidator.validar(mananaDespues, null));

        // Tarde empieza antes de 13:00
        HorarioDto tardeAntes = new HorarioDto(cursoTarde.getId(), "Lunes", LocalTime.of(12, 45), LocalTime.of(14, 15),
                fisica.getId(), docenteAna.getId(), null, "Aula 201");
        assertThrows(IllegalArgumentException.class, () -> horarioValidator.validar(tardeAntes, null));

        // Tarde termina después de 18:00
        HorarioDto tardeDespues = new HorarioDto(cursoTarde.getId(), "Lunes", LocalTime.of(17, 0), LocalTime.of(18, 30),
                fisica.getId(), docenteAna.getId(), null, "Aula 201");
        assertThrows(IllegalArgumentException.class, () -> horarioValidator.validar(tardeDespues, null));
    }

    @Test
    @DisplayName("3. Franja exactamente en el borde de jornada se acepta (07:00-08:00 y 11:00-12:00)")
    void testFranjaExactaEnBordesDeJornadaSeAcepta() {
        // Mañana: borde inferior 07:00 - 08:00
        HorarioDto inicioManana = new HorarioDto(cursoManana.getId(), "Lunes", LocalTime.of(7, 0), LocalTime.of(8, 0),
                matematicas.getId(), docenteCarlos.getId(), null, "Aula 101");
        assertDoesNotThrow(() -> horarioValidator.validar(inicioManana, null));

        // Mañana: borde superior 11:00 - 12:00
        HorarioDto finManana = new HorarioDto(cursoManana.getId(), "Martes", LocalTime.of(11, 0), LocalTime.of(12, 0),
                matematicas.getId(), docenteCarlos.getId(), null, "Aula 101");
        assertDoesNotThrow(() -> horarioValidator.validar(finManana, null));

        // Tarde: borde inferior 13:00 - 14:00 y superior 17:00 - 18:00
        HorarioDto inicioTarde = new HorarioDto(cursoTarde.getId(), "Lunes", LocalTime.of(13, 0), LocalTime.of(14, 0),
                fisica.getId(), docenteAna.getId(), null, "Aula 201");
        assertDoesNotThrow(() -> horarioValidator.validar(inicioTarde, null));

        HorarioDto finTarde = new HorarioDto(cursoTarde.getId(), "Martes", LocalTime.of(17, 0), LocalTime.of(18, 0),
                fisica.getId(), docenteAna.getId(), null, "Aula 201");
        assertDoesNotThrow(() -> horarioValidator.validar(finTarde, null));
    }

    @Test
    @DisplayName("3b. Tolerancia explícita permite extensión de jornada configurada")
    void testToleranciaExplicitaPermiteExtension() {
        // Sin tolerancia, clase terminando a las 12:10 se rechaza
        HorarioDto conExtension = new HorarioDto(cursoManana.getId(), "Lunes", LocalTime.of(10, 30), LocalTime.of(12, 10),
                matematicas.getId(), docenteCarlos.getId(), null, "Aula 101");
        assertThrows(IllegalArgumentException.class, () -> horarioValidator.validar(conExtension, null));

        // Con parámetro de tolerancia explícito de 10 minutos, se acepta
        assertDoesNotThrow(() -> horarioValidator.validar(conExtension, null, Duration.ofMinutes(10)));
    }

    @Test
    @DisplayName("4. Choque por docente: 08:00-09:00 contra 08:30-09:30 se rechaza (join Horario -> CursoMateria)")
    void testChoquePorDocenteSeRechaza() {
        // Guardar primer horario con docenteCarlos en cursoManana
        cursoService.guardarHorarioBloque(cursoManana.getId(), "Lunes", matematicas.getId(), docenteCarlos.getId(),
                "08:00", "09:00", "Aula 101");

        // Intentar programar a docenteCarlos en otro curso a las 08:30 - 09:30
        Curso otroCurso = new Curso();
        otroCurso.setGrado("11°");
        otroCurso.setGrupo("01");
        otroCurso.setJornada("Mañana");
        otroCurso.setAnoLectivo("2026");
        otroCurso = cursoRepository.save(otroCurso);

        HorarioDto choqueDocente = new HorarioDto(otroCurso.getId(), "Lunes", LocalTime.of(8, 30), LocalTime.of(9, 30),
                fisica.getId(), docenteCarlos.getId(), null, "Aula 202");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                horarioValidator.validar(choqueDocente, null));
        assertTrue(ex.getMessage().contains("Conflicto de horario: El docente ya tiene clase"));
    }

    @Test
    @DisplayName("5. Choque por salón y por curso con la misma estructura")
    void testChoquePorSalonYCurso() {
        // Horario en Aula 101 para cursoManana a las 08:00-09:00
        cursoService.guardarHorarioBloque(cursoManana.getId(), "Miércoles", matematicas.getId(), docenteCarlos.getId(),
                "08:00", "09:00", "Aula 101");

        Curso otroCurso = new Curso();
        otroCurso.setGrado("11°");
        otroCurso.setGrupo("02");
        otroCurso.setJornada("Mañana");
        otroCurso.setAnoLectivo("2026");
        otroCurso = cursoRepository.save(otroCurso);

        // Choque por salón: otro curso intenta usar Aula 101 de 08:30 a 09:30
        HorarioDto choqueSalon = new HorarioDto(otroCurso.getId(), "Miércoles", LocalTime.of(8, 30), LocalTime.of(9, 30),
                fisica.getId(), docenteAna.getId(), null, "Aula 101");
        IllegalArgumentException exSalon = assertThrows(IllegalArgumentException.class, () ->
                horarioValidator.validar(choqueSalon, null));
        assertTrue(exSalon.getMessage().contains("Conflicto de salón"));

        // Choque por curso: el mismo cursoManana intenta tener otra clase en otro salón de 08:30 a 09:30
        HorarioDto choqueCurso = new HorarioDto(cursoManana.getId(), "Miércoles", LocalTime.of(8, 30), LocalTime.of(9, 30),
                fisica.getId(), docenteAna.getId(), null, "Laboratorio");
        IllegalArgumentException exCurso = assertThrows(IllegalArgumentException.class, () ->
                horarioValidator.validar(choqueCurso, null));
        assertTrue(exCurso.getMessage().contains("Conflicto de curso"));
    }

    @Test
    @DisplayName("6. Borde contiguo: 08:00-08:40 y 08:40-09:30 se aceptan sin conflicto")
    void testBordeContiguoSeAcepta() {
        // Verificar matemáticamente la regla pura
        assertFalse(HorarioValidator.seSolapan(
                LocalTime.of(8, 0), LocalTime.of(8, 40),
                LocalTime.of(8, 40), LocalTime.of(9, 30)
        ));

        // En repositorio y servicio
        cursoService.guardarHorarioBloque(cursoManana.getId(), "Jueves", matematicas.getId(), docenteCarlos.getId(),
                "08:00", "08:40", "Aula 101");

        HorarioDto contiguo = new HorarioDto(cursoManana.getId(), "Jueves", LocalTime.of(8, 40), LocalTime.of(9, 30),
                matematicas.getId(), docenteCarlos.getId(), null, "Aula 101");

        assertDoesNotThrow(() -> horarioValidator.validar(contiguo, null));
    }

    @Test
    @DisplayName("7. Franja contenida completamente dentro de otra se rechaza")
    void testFranjaContenidaCompletamenteSeRechaza() {
        // Padre: 08:00 a 10:00
        cursoService.guardarHorarioBloque(cursoManana.getId(), "Viernes", matematicas.getId(), docenteCarlos.getId(),
                "08:00", "10:00", "Aula 101");

        // Contenida: 08:30 a 09:30
        HorarioDto contenida = new HorarioDto(cursoManana.getId(), "Viernes", LocalTime.of(8, 30), LocalTime.of(9, 30),
                fisica.getId(), docenteAna.getId(), null, "Aula 101");

        assertThrows(IllegalArgumentException.class, () -> horarioValidator.validar(contenida, null));
    }

    @Test
    @DisplayName("8. Editar un horario sin cambiar sus horas no choca consigo mismo")
    void testEditarHorarioSinCambiarHorasNoChocaConsigoMismo() {
        Horario h = cursoService.guardarHorarioBloque(cursoManana.getId(), "Lunes", matematicas.getId(), docenteCarlos.getId(),
                "09:00", "10:00", "Aula 101");

        // Editar cambiando el salón o manteniendo exactamente las mismas horas
        assertDoesNotThrow(() -> cursoService.actualizarHorario(
                h.getId(), cursoManana.getId(), "Lunes", matematicas.getId(), docenteCarlos.getId(),
                "09:00", "10:00", "Aula 102"
        ));

        Horario actualizado = horarioRepository.findById(h.getId()).orElseThrow();
        assertEquals("Aula 102", actualizado.getSalon());
        assertEquals(LocalTime.of(9, 0), actualizado.getHoraInicio());
    }

    @Test
    @DisplayName("9. Mismo docente y horas iguales en otro día o en otro año lectivo se acepta")
    void testMismoDocenteHorasIgualesOtroDiaUAnioSeAcepta() {
        cursoService.guardarHorarioBloque(cursoManana.getId(), "Lunes", matematicas.getId(), docenteCarlos.getId(),
                "07:00", "08:30", "Aula 101");

        // Mismo docente, misma hora, pero en MARTES -> Aceptado
        HorarioDto otroDia = new HorarioDto(cursoManana.getId(), "Martes", LocalTime.of(7, 0), LocalTime.of(8, 30),
                matematicas.getId(), docenteCarlos.getId(), null, "Aula 101");
        assertDoesNotThrow(() -> horarioValidator.validar(otroDia, null));

        // Mismo docente, misma hora y mismo día, pero en curso de otro AÑO LECTIVO (ej. 2025) -> Aceptado
        Curso curso2025 = new Curso();
        curso2025.setGrado("10°");
        curso2025.setGrupo("01");
        curso2025.setJornada("Mañana");
        curso2025.setAnoLectivo("2025");
        curso2025 = cursoRepository.save(curso2025);

        HorarioDto otroAnio = new HorarioDto(curso2025.getId(), "Lunes", LocalTime.of(7, 0), LocalTime.of(8, 30),
                matematicas.getId(), docenteCarlos.getId(), null, "Aula 101");
        assertDoesNotThrow(() -> horarioValidator.validar(otroAnio, null));
    }

    @Test
    @DisplayName("10. La suma de horas frente a la intensidad de la materia genera advertencia, no error")
    void testSuperarIntensidadGeneraAdvertenciaNoBloqueante() {
        // fisica tiene intensidadHoraria = 2 (2 horas semanales)
        // Guardamos 2 horas en Lunes (08:00 a 10:00)
        cursoService.guardarHorarioBloque(cursoManana.getId(), "Lunes", fisica.getId(), docenteAna.getId(),
                "08:00", "10:00", "Lab-1");

        // Intentamos programar 1 hora más el Martes (08:00 a 09:00) -> 3 horas > 2 horas de intensidad
        HorarioDto excedeIntensidad = new HorarioDto(cursoManana.getId(), "Martes", LocalTime.of(8, 0), LocalTime.of(9, 0),
                fisica.getId(), docenteAna.getId(), null, "Lab-1");

        // NO debe lanzar excepción
        HorarioValidator.ValidacionResultado resultado = assertDoesNotThrow(() ->
                horarioValidator.validar(excedeIntensidad, null));

        assertTrue(resultado.valido());
        assertTrue(resultado.tieneAdvertencias());
        assertTrue(resultado.advertencias().get(0).contains("superan la intensidad sugerida"));
    }
}
