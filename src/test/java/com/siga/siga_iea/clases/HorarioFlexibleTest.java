package com.siga.siga_iea.clases;

import com.siga.siga_iea.clases.entity.Curso;
import com.siga.siga_iea.clases.entity.Horario;
import com.siga.siga_iea.clases.entity.Materia;
import com.siga.siga_iea.clases.repository.CursoRepository;
import com.siga.siga_iea.clases.repository.HorarioRepository;
import com.siga.siga_iea.clases.repository.MateriaRepository;
import com.siga.siga_iea.clases.service.CursoService;
import com.siga.siga_iea.usuarios.entity.Docente;
import com.siga.siga_iea.usuarios.repository.DocenteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
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
    private CursoService cursoService;

    @Test
    @Transactional
    @DisplayName("Debe permitir programar horarios con horas arbitrarias y flexibles estilo Google Calendar")
    void testProgramacionHorasFlexibles() {
        Curso curso = new Curso();
        curso.setGrado("9°");
        curso.setGrupo("01");
        curso.setJornada("Mañana");
        curso.setAnoLectivo("2026");
        curso = cursoRepository.save(curso);

        Materia materia = new Materia("Filosofía y Ética", "FIL-101");
        materia = materiaRepository.save(materia);

        Docente docente = new Docente();
        docente.setNombres("Alejandro");
        docente.setApellidos("Morales");
        docente.setNumeroDocumento("99887766");
        docente = docenteRepository.save(docente);

        // Guardar con horas arbitrarias libres (ej. 07:15 a 08:45)
        Horario h1 = cursoService.guardarHorarioBloque(
                curso.getId(), "Lunes", materia.getId(), docente.getId(),
                "07:15", "08:45", "Aula 101"
        );

        assertNotNull(h1.getId());
        assertEquals(LocalTime.of(7, 15), h1.getHoraInicio());
        assertEquals(LocalTime.of(8, 45), h1.getHoraFin());
        assertEquals("Lunes", h1.getDiaSemana());
        assertEquals("Aula 101", h1.getSalon());

        // Guardar otro bloque con hora atípica (ej. 09:10 a 10:25)
        Horario h2 = cursoService.guardarHorarioBloque(
                curso.getId(), "Lunes", materia.getId(), docente.getId(),
                "09:10", "10:25", "Laboratorio 1"
        );

        List<Horario> horarios = cursoService.listarHorariosDeCurso(curso.getId());
        assertEquals(2, horarios.size());

        // Actualizar el horario h1 con nuevas horas (07:30 a 09:00)
        Horario actualizado = cursoService.actualizarHorario(
                h1.getId(), curso.getId(), "Lunes", materia.getId(), docente.getId(),
                "07:30", "09:00", "Sala de Informática"
        );

        assertEquals(LocalTime.of(7, 30), actualizado.getHoraInicio());
        assertEquals(LocalTime.of(9, 0), actualizado.getHoraFin());
        assertEquals("Sala de Informática", actualizado.getSalon());

        // Eliminar un horario
        cursoService.eliminarHorario(actualizado.getId());
        List<Horario> postEliminar = cursoService.listarHorariosDeCurso(curso.getId());
        assertEquals(1, postEliminar.size());

        // Limpiar todos
        cursoService.limpiarHorarioCurso(curso.getId());
        assertTrue(cursoService.listarHorariosDeCurso(curso.getId()).isEmpty());
    }
}
