package com.siga.siga_iea.clases;

import com.siga.siga_iea.asistencias.service.AsistenciaService;
import com.siga.siga_iea.clases.entity.Curso;
import com.siga.siga_iea.clases.enums.GradoAcademico;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias de Entidad Curso y GradoAcademico")
class CursoTest {

    @ParameterizedTest
    @DisplayName("Grado Transición / Preescolar se normaliza canónicamente a 'Transición'")
    @ValueSource(strings = {"Transición", "transición", "transicion", "TRANSICION", "0", "0°", "Preescolar"})
    void testGradoTransicionNormalizado(String entrada) {
        Curso curso = new Curso();
        curso.setGrado(entrada);

        assertEquals("Transición", curso.getGrado(),
                "Debe normalizar a 'Transición' canónico sin perder caracteres alfabéticos");
        assertEquals(GradoAcademico.TRANSICION, curso.getGradoAcademico());
        assertTrue(curso.getGradoAcademico().esPrimariaOPreescolar());
    }

    @ParameterizedTest
    @DisplayName("Grados numéricos se formatean con sufijo de grado canónico (°)")
    @CsvSource({
            "1, 1°",
            "1°, 1°",
            "5, 5°",
            "6, 6°",
            "9, 9°",
            "10, 10°",
            "11, 11°",
            " 11° , 11°"
    })
    void testGradosNumericosFormateadosConSufijo(String entrada, String esperado) {
        Curso curso = new Curso();
        curso.setGrado(entrada);

        assertEquals(esperado, curso.getGrado());
    }

    @ParameterizedTest
    @DisplayName("Valores nulos, vacíos o en blanco rechazan la asignación con IllegalArgumentException (sin default silencioso)")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n", "Invalido", "Grado Desconocido", "12°", "-1"})
    void testValoresInvalidosLanzanExcepcion(String valorInvalido) {
        Curso curso = new Curso();
        assertThrows(IllegalArgumentException.class, () -> curso.setGrado(valorInvalido),
                "No debe aceptar grados fuera del catálogo ni asignar '11°' por defecto");
    }

    @Test
    @DisplayName("getCodigoCurso genera identificador canónico para grados numéricos y Transición")
    void testCodigoCursoGeneracion() {
        Curso cursoNumerico = new Curso();
        cursoNumerico.setGrado("10°");
        cursoNumerico.setGrupo("02");
        assertEquals("10-02", cursoNumerico.getCodigoCurso());

        Curso cursoTransicion = new Curso();
        cursoTransicion.setGrado("Transición");
        cursoTransicion.setGrupo("01");
        assertEquals("TRANS-01", cursoTransicion.getCodigoCurso());
    }

    @Test
    @DisplayName("Transición es reconocido como Preescolar/Primaria por AsistenciaService")
    void testCursoTransicionReconocidoComoPrimariaOPreescolar() {
        Curso curso = new Curso();
        curso.setGrado("Transición");

        assertTrue(AsistenciaService.esPrimariaOPreescolar(curso.getGrado()),
                "El curso con grado Transición debe ser clasificado como Primaria/Preescolar para generar sesión tipo JORNADA");
    }
}
