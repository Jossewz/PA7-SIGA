package com.siga.siga_iea.matricula;

import com.siga.siga_iea.clases.entity.Curso;
import com.siga.siga_iea.clases.entity.CursoEstudiante;
import com.siga.siga_iea.clases.repository.CursoEstudianteRepository;
import com.siga.siga_iea.clases.repository.CursoRepository;
import com.siga.siga_iea.matricula.entity.Matricula;
import com.siga.siga_iea.matricula.repository.MatriculaRepository;
import com.siga.siga_iea.matricula.service.MatriculaService;
import com.siga.siga_iea.usuarios.entity.Estudiante;
import com.siga.siga_iea.usuarios.repository.EstudianteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MatriculaCursoSincronizacionTest {

    @Autowired
    private MatriculaRepository matriculaRepository;

    @Autowired
    private MatriculaService matriculaService;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private CursoEstudianteRepository cursoEstudianteRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Test
    @Transactional
    @DisplayName("La aprobación de matrícula debe asociar el curso y crear CursoEstudiante en la misma transacción")
    void testAprobacionMatriculaCreaCursoEstudiante() {
        // 1. Crear estudiante
        Estudiante est = new Estudiante();
        est.setNombres("Mateo");
        est.setApellidos("Herrera");
        est.setNumeroDocumento("1033445566");
        est.setEstado("Inactivo");
        est = estudianteRepository.save(est);

        // 2. Crear curso destino
        Curso curso = new Curso();
        curso.setGrado("9°");
        curso.setGrupo("01");
        curso.setJornada("Mañana");
        curso.setAnoLectivo("2026");
        curso.setCuposMaximos(35);
        curso = cursoRepository.save(curso);

        // 3. Crear matrícula pendiente
        Matricula matricula = new Matricula();
        matricula.setEstudiante(est);
        matricula.setGrado("9°");
        matricula.setSalon("01");
        matricula.setAnoLectivo("2026");
        matricula.setEstado("PENDIENTE_DE_REVISION");
        matricula.setFechaMatricula(LocalDate.now());
        matricula.setAutorizaTratamientoDatos(true);
        matricula.setAutorizadoPorNombre("Acudiente de Mateo");
        matricula.setAutorizadoPorDocumento("71223344");
        matricula = matriculaRepository.save(matricula);

        assertNull(matricula.getCurso(), "Inicialmente no debe tener curso asociado");

        // 4. Ejecutar aprobación transaccional
        Matricula matriculaAprobada = matriculaService.aprobarMatricula(matricula.getId(), curso.getId());

        // 5. Verificaciones
        assertEquals("APROBADA", matriculaAprobada.getEstado());
        assertNotNull(matriculaAprobada.getCurso());
        assertEquals(curso.getId(), matriculaAprobada.getCurso().getId());
        assertTrue(matriculaAprobada.getAutorizaTratamientoDatos());
        assertNotNull(matriculaAprobada.getFechaAutorizacionDatos());

        // Verificar que CursoEstudiante fue creado atómicamente
        Optional<CursoEstudiante> ceOpt = cursoEstudianteRepository.findByEstudianteIdAndAnoLectivo(est.getId(), "2026");
        assertTrue(ceOpt.isPresent(), "Debe existir registro en curso_estudiante para el estudiante y año 2026");
        assertEquals(curso.getId(), ceOpt.get().getCurso().getId(), "CursoEstudiante debe apuntar exactamente al curso asignado");

        // Verificar que el estudiante quedó Activo
        assertEquals("Activo", est.getEstado());
    }

    @Test
    @Transactional
    @DisplayName("El traslado de curso debe actualizar Matricula y CursoEstudiante atómicamente")
    void testTrasladoActualizaMatriculaYCursoEstudiante() {
        // 1. Estudiante y cursos A y B
        Estudiante est = new Estudiante();
        est.setNombres("Valentina");
        est.setApellidos("Castro");
        est.setNumeroDocumento("1044556677");
        est = estudianteRepository.save(est);

        Curso cursoA = new Curso();
        cursoA.setGrado("9°");
        cursoA.setGrupo("01");
        cursoA.setJornada("Mañana");
        cursoA.setAnoLectivo("2026");
        cursoA = cursoRepository.save(cursoA);

        Curso cursoB = new Curso();
        cursoB.setGrado("9°");
        cursoB.setGrupo("02");
        cursoB.setJornada("Tarde");
        cursoB.setAnoLectivo("2026");
        cursoB = cursoRepository.save(cursoB);

        // 2. Matrícula aprobada en Curso A
        Matricula matricula = new Matricula();
        matricula.setEstudiante(est);
        matricula.setGrado("9°");
        matricula.setSalon("01");
        matricula.setAnoLectivo("2026");
        matricula.setEstado("PENDIENTE_DE_REVISION");
        matricula.setFechaMatricula(LocalDate.now());
        matricula.setAutorizadoPorNombre("Acudiente de Valentina");
        matricula.setAutorizadoPorDocumento("71334455");
        matricula = matriculaRepository.save(matricula);

        matriculaService.aprobarMatricula(matricula.getId(), cursoA.getId());

        // 3. Trasladar a Curso B
        Matricula matriculaTrasladada = matriculaService.trasladarEstudiante(matricula.getId(), cursoB.getId());

        // 4. Verificaciones
        assertEquals(cursoB.getId(), matriculaTrasladada.getCurso().getId(), "Matrícula debe apuntar a curso B");
        assertEquals("02", matriculaTrasladada.getSalon(), "Salón debe haberse actualizado al grupo del nuevo curso");

        Optional<CursoEstudiante> ceActualizado = cursoEstudianteRepository.findByEstudianteIdAndAnoLectivo(est.getId(), "2026");
        assertTrue(ceActualizado.isPresent());
        assertEquals(cursoB.getId(), ceActualizado.get().getCurso().getId(), "CursoEstudiante debe haber sido migrado al curso B");
    }

    @Test
    @Transactional
    @DisplayName("Aprobar una matrícula con autorización marcada y sin nombre o documento debe fallar (Ley 1581)")
    void testAprobacionSinDatosAutorizanteFallaLey1581() {
        Estudiante est = new Estudiante();
        est.setNombres("Camilo");
        est.setApellidos("Torres");
        est.setNumeroDocumento("1055667788");
        est = estudianteRepository.save(est);

        Curso curso = new Curso();
        curso.setGrado("9°");
        curso.setGrupo("01");
        curso.setJornada("Mañana");
        curso.setAnoLectivo("2026");
        curso = cursoRepository.save(curso);
        final UUID cursoId = curso.getId();

        // Caso 1: Sin nombre de autorizante
        Matricula mSinNombre = new Matricula();
        mSinNombre.setEstudiante(est);
        mSinNombre.setGrado("9°");
        mSinNombre.setAnoLectivo("2026");
        mSinNombre.setEstado("PENDIENTE_DE_REVISION");
        mSinNombre.setAutorizaTratamientoDatos(true);
        mSinNombre.setAutorizadoPorNombre(null);
        mSinNombre.setAutorizadoPorDocumento("12345678");
        mSinNombre = matriculaRepository.save(mSinNombre);

        final UUID mSinNombreId = mSinNombre.getId();
        IllegalStateException ex1 = assertThrows(IllegalStateException.class, () ->
                matriculaService.aprobarMatricula(mSinNombreId, cursoId));
        assertTrue(ex1.getMessage().contains("nombre del autorizante es obligatorio"));

        // Caso 2: Sin documento de autorizante
        Matricula mSinDoc = new Matricula();
        mSinDoc.setEstudiante(est);
        mSinDoc.setGrado("9°");
        mSinDoc.setAnoLectivo("2026");
        mSinDoc.setEstado("PENDIENTE_DE_REVISION");
        mSinDoc.setAutorizaTratamientoDatos(true);
        mSinDoc.setAutorizadoPorNombre("Acudiente Principal");
        mSinDoc.setAutorizadoPorDocumento("");
        mSinDoc = matriculaRepository.save(mSinDoc);

        final UUID mSinDocId = mSinDoc.getId();
        IllegalStateException ex2 = assertThrows(IllegalStateException.class, () ->
                matriculaService.aprobarMatricula(mSinDocId, cursoId));
        assertTrue(ex2.getMessage().contains("documento del autorizante es obligatorio"));
    }
}
