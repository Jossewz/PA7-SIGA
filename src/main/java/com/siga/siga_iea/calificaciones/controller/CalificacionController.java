package com.siga.siga_iea.calificaciones.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CalificacionController {

    @GetMapping("/calificaciones")
    public String index(Model model) {
        model.addAttribute("title", "Boletín de Calificaciones – IEACI");
        model.addAttribute("activePage", "calificaciones");

        // Student Info
        model.addAttribute("estudianteNombre", "Mateo Álvarez Restrepo");
        model.addAttribute("estudianteGrado", "11° - 01");
        model.addAttribute("estudianteDocumento", "1098432101");

        // Mock subject list with 3 periods (P1, P2, P3) and evaluation lists for each period
        List<Map<String, Object>> asignaturas = new ArrayList<>();
        
        asignaturas.add(createAsignatura("Matemáticas", "Prof. Jorge Eliécer Rojas", 4.38, 4.23, 4.50, 4.37,
            List.of(
                createEvaluación("Evaluación Escrita - Ágebra", "25%", 4.5),
                createEvaluación("Taller de Funciones", "25%", 4.0),
                createEvaluación("Trabajo de Campo", "25%", 4.8),
                createEvaluación("Examen Parcial", "25%", 4.2)
            ),
            List.of(
                createEvaluación("Geometría Analítica", "35%", 4.2),
                createEvaluación("Trigonometría Avanzada", "35%", 4.5),
                createEvaluación("Quiz de Repaso", "30%", 4.0)
            ),
            List.of(
                createEvaluación("Cálculo Diferencial", "50%", 4.6),
                createEvaluación("Evaluación Final", "50%", 4.4)
            )
        ));

        asignaturas.add(createAsignatura("Lengua Castellana", "Prof. Ana María Sánchez", 4.13, 4.00, 4.20, 4.11,
            List.of(
                createEvaluación("Análisis Literario", "25%", 3.8),
                createEvaluación("Ensayo Argumentativo", "25%", 4.2),
                createEvaluación("Ortografía y Redacción", "25%", 4.0),
                createEvaluación("Examen de Período", "25%", 4.5)
            ),
            List.of(
                createEvaluación("Literatura Hispanoamericana", "40%", 4.0),
                createEvaluación("Taller de Redacción", "60%", 4.0)
            ),
            List.of(
                createEvaluación("Comprensión Lectora", "50%", 4.2),
                createEvaluación("Prueba Saber", "50%", 4.2)
            )
        ));

        asignaturas.add(createAsignatura("Ciencias Naturales (Física)", "Prof. Carlos Mendoza", 4.00, 4.30, 4.40, 4.23,
            List.of(
                createEvaluación("Laboratorio Cinemática", "35%", 4.0),
                createEvaluación("Guía de Ejercicios", "35%", 3.8),
                createEvaluación("Evaluación Final", "30%", 4.2)
            ),
            List.of(
                createEvaluación("Dinámica y Fuerza", "50%", 4.3),
                createEvaluación("Taller de Vectores", "50%", 4.3)
            ),
            List.of(
                createEvaluación("Termodinámica", "50%", 4.4),
                createEvaluación("Proyecto Científico", "50%", 4.4)
            )
        ));

        asignaturas.add(createAsignatura("Ciencias Sociales", "Prof. Luis Felipe Gómez", 4.65, 4.50, 4.80, 4.65,
            List.of(
                createEvaluación("Exposición de Historia", "30%", 4.8),
                createEvaluación("Ensayo de Geografía", "30%", 4.6),
                createEvaluación("Participación y Quices", "40%", 4.5)
            ),
            List.of(
                createEvaluación("Constitución Política", "50%", 4.5),
                createEvaluación("Mesa Redonda", "50%", 4.5)
            ),
            List.of(
                createEvaluación("Geopolítica Mundial", "50%", 4.8),
                createEvaluación("Examen de Período", "50%", 4.8)
            )
        ));

        asignaturas.add(createAsignatura("Inglés", "Prof. Patricia López", 4.50, 4.60, 4.70, 4.60,
            List.of(
                createEvaluación("Listening & Speaking Test", "35%", 4.2),
                createEvaluación("Grammar Quiz", "35%", 4.5),
                createEvaluación("Reading Project", "30%", 4.8)
            ),
            List.of(
                createEvaluación("Vocabulary Test", "50%", 4.6),
                createEvaluación("Oral Presentation", "50%", 4.6)
            ),
            List.of(
                createEvaluación("Essay Writing", "50%", 4.7),
                createEvaluación("Final Speaking Exam", "50%", 4.7)
            )
        ));

        model.addAttribute("asignaturas", asignaturas);
        return "calificaciones/index";
    }

    private Map<String, Object> createAsignatura(String nombre, String docente, double p1, double p2, double p3, double definitiva, 
                                                 List<Map<String, Object>> evalP1, List<Map<String, Object>> evalP2, List<Map<String, Object>> evalP3) {
        Map<String, Object> a = new HashMap<>();
        a.put("nombre", nombre);
        a.put("docente", docente);
        a.put("p1", p1);
        a.put("p2", p2);
        a.put("p3", p3);
        a.put("definitiva", definitiva);
        a.put("evalP1", evalP1);
        a.put("evalP2", evalP2);
        a.put("evalP3", evalP3);
        return a;
    }

    private Map<String, Object> createEvaluación(String tema, String peso, double nota) {
        Map<String, Object> ev = new HashMap<>();
        ev.put("tema", tema);
        ev.put("peso", peso);
        ev.put("nota", nota);
        return ev;
    }
}
