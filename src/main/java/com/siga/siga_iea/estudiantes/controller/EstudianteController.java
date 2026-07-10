package com.siga.siga_iea.estudiantes.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class EstudianteController {

    @GetMapping("/estudiantes")
    public String index(Model model) {
        model.addAttribute("title", "Gestión de Estudiantes – IEACI");
        model.addAttribute("activePage", "estudiantes");

        // Mock list of students
        List<Map<String, Object>> estudiantesList = new ArrayList<>();
        estudiantesList.add(createStudent("1", "2026001", "Santiago Alejandro", "Gómez Pérez", "9° - A", "Activo"));
        estudiantesList.add(createStudent("2", "2026002", "Valeria Sofía", "Rodríguez Silva", "10° - A", "Activo"));
        estudiantesList.add(createStudent("3", "2026003", "Mateo David", "Hernández Ruiz", "9° - A", "Activo"));
        estudiantesList.add(createStudent("4", "2026004", "Isabella", "López Castro", "11° - A", "Activo"));
        estudiantesList.add(createStudent("5", "2026005", "Juan Sebastian", "Pérez Díaz", "8° - B", "Retirado"));

        model.addAttribute("estudiantesList", estudiantesList);
        return "estudiantes/index";
    }

    @GetMapping("/estudiantes/perfil/{id}")
    public String perfil(@PathVariable String id, Model model) {
        model.addAttribute("title", "Perfil del Estudiante – IEACI");
        model.addAttribute("activePage", "estudiantes");

        // Mock individual details
        Map<String, Object> student = null;
        if ("2".equals(id)) {
            student = createStudent("2", "2026002", "Valeria Sofía", "Rodríguez Silva", "10° - A", "Activo");
            student.put("documento", "TI - 1045328901");
            student.put("fechaNac", "2010-04-14");
            student.put("edad", "16 años");
            student.put("sexo", "Femenino");
            
            // Parent info
            student.put("acudienteNombre", "Martha Cecilia Silva");
            student.put("acudienteParentesco", "Madre");
            student.put("acudienteDoc", "CC - 45.321.456");
            student.put("acudienteTel", "311 456 7890");
            student.put("acudienteDir", "Calle 45 # 12 - 34");
            
            // Health info
            student.put("saludEps", "SURA");
            student.put("saludRh", "O+");
            student.put("saludAlergias", "Ninguna registrada");
            student.put("saludObs", "Apta para educación física");
        } else {
            student = createStudent("1", "2026001", "Santiago Alejandro", "Gómez Pérez", "9° - A", "Activo");
            student.put("documento", "TI - 1088456123");
            student.put("fechaNac", "2011-08-25");
            student.put("edad", "14 años");
            student.put("sexo", "Masculino");
            
            // Parent info
            student.put("acudienteNombre", "Felipe Gómez Rivas");
            student.put("acudienteParentesco", "Padre");
            student.put("acudienteDoc", "CC - 73.123.456");
            student.put("acudienteTel", "300 123 4567");
            student.put("acudienteDir", "Manzana A Lote 5, San José");
            
            // Health info
            student.put("saludEps", "Coosalud");
            student.put("saludRh", "A+");
            student.put("saludAlergias", "Polen y Acaros");
            student.put("saludObs", "Ninguna");
        }

        model.addAttribute("student", student);
        return "estudiantes/perfil";
    }

    private Map<String, Object> createStudent(String id, String codigo, String nombres, String apellidos, String curso, String estado) {
        Map<String, Object> s = new HashMap<>();
        s.put("id", id);
        s.put("codigo", codigo);
        s.put("nombres", nombres);
        s.put("apellidos", apellidos);
        s.put("nombreCompleto", nombres + " " + apellidos);
        s.put("curso", curso);
        s.put("estado", estado);
        return s;
    }
}
