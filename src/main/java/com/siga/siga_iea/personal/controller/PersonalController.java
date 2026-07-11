package com.siga.siga_iea.personal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PersonalController {

    @GetMapping("/personal")
    public String index(Model model) {
        model.addAttribute("title", "Gestión de Personal – IEACI");
        model.addAttribute("activePage", "personal");

        // Mock data for personal
        List<Map<String, Object>> personalList = new ArrayList<>();
        
        personalList.add(createStaff("1", "Carlos", "Mendoza", "Docente", "Académica", "Activo", "carlos.mendoza@ieaci.edu.co", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=120&h=120&q=80"));
        personalList.add(createStaff("2", "Ana María", "Sánchez", "Coordinador", "Académica", "Activo", "ana.sanchez@ieaci.edu.co", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=120&h=120&q=80"));
        personalList.add(createStaff("3", "Luis Felipe", "Gómez", "Secretario", "Secretaría", "Activo", "luis.gomez@ieaci.edu.co", "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&w=120&h=120&q=80"));
        personalList.add(createStaff("4", "Patricia", "López", "Bibliotecario", "Administrativa", "Inactivo", "patricia.lopez@ieaci.edu.co", "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=120&h=120&q=80"));
        personalList.add(createStaff("5", "Jorge Eliécer", "Rojas", "Rector", "Administrativa", "Activo", "jorge.rojas@ieaci.edu.co", "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?auto=format&fit=crop&w=120&h=120&q=80"));

        model.addAttribute("personalList", personalList);
        return "personal/index";
    }

    @GetMapping("/personal/perfil/{id}")
    public String perfil(@PathVariable String id, Model model) {
        model.addAttribute("title", "Perfil de Personal – IEACI");
        model.addAttribute("activePage", "personal");
        
        // Mock individual details
        Map<String, Object> staff = null;
        if ("2".equals(id)) {
            staff = createStaff("2", "Ana María", "Sánchez", "Coordinador", "Académica", "Activo", "ana.sanchez@ieaci.edu.co", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=120&h=120&q=80");
            staff.put("segundoNombre", "María");
            staff.put("segundoApellido", "Gutiérrez");
            staff.put("tipoDoc", "Cédula de Ciudadanía");
            staff.put("numDoc", "1.045.321.456");
            staff.put("fechaNac", "1988-11-23");
            staff.put("sexo", "Femenino");
            staff.put("estadoCivil", "Casada");
            staff.put("nacionalidad", "Colombiana");
            staff.put("telefono", "311 456 7890");
            staff.put("direccion", "Calle 45 # 12 - 34");
            staff.put("ciudad", "Cartagena");
            staff.put("tipoContrato", "Término Indefinido");
            staff.put("jornada", "Completa");
            staff.put("fechaIngreso", "2018-01-15");
            staff.put("salario", "$ 3.500.000");
            staff.put("profesion", "Licenciada en Educación");
            staff.put("especialidad", "Gestión Educativa");
            staff.put("nivelEducativo", "Maestría");
            staff.put("universidad", "Universidad de Cartagena");
        } else {
            staff = createStaff("1", "Carlos", "Mendoza", "Docente", "Académica", "Activo", "carlos.mendoza@ieaci.edu.co", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=120&h=120&q=80");
            staff.put("segundoNombre", "Antonio");
            staff.put("segundoApellido", "Ortega");
            staff.put("tipoDoc", "Cédula de Ciudadanía");
            staff.put("numDoc", "1.088.123.456");
            staff.put("fechaNac", "1985-05-12");
            staff.put("sexo", "Masculino");
            staff.put("estadoCivil", "Soltero");
            staff.put("nacionalidad", "Colombiana");
            staff.put("telefono", "300 123 4567");
            staff.put("direccion", "Manzana A Lote 5, San José");
            staff.put("ciudad", "Cartagena");
            staff.put("tipoContrato", "Término Fijo (10 meses)");
            staff.put("jornada", "Mañana");
            staff.put("fechaIngreso", "2021-02-01");
            staff.put("salario", "$ 2.200.000");
            staff.put("profesion", "Licenciado en Matemáticas");
            staff.put("especialidad", "Álgebra y Cálculo");
            staff.put("nivelEducativo", "Especialización");
            staff.put("universidad", "Universidad del Atlántico");
        }

        model.addAttribute("staff", staff);
        model.addAttribute("activeTab", "informacion");
        return "personal/perfil";
    }

    private Map<String, Object> createStaff(String id, String nombre, String apellido, String cargo, String area, String estado, String correo, String foto) {
        Map<String, Object> staff = new HashMap<>();
        staff.put("id", id);
        staff.put("primerNombre", nombre);
        staff.put("primerApellido", apellido);
        staff.put("nombreCompleto", nombre + " " + apellido);
        staff.put("cargo", cargo);
        staff.put("area", area);
        staff.put("estado", estado);
        staff.put("correo", correo);
        staff.put("foto", foto);
        return staff;
    }
}
