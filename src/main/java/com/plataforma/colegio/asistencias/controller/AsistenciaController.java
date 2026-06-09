package com.plataforma.colegio.asistencias.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AsistenciaController {
    @GetMapping("/asistencias")
    public String index(Model model) {
        return "asistencias/index";
    }
}
