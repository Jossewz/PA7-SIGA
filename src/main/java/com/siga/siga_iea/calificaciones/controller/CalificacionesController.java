package com.siga.siga_iea.calificaciones.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CalificacionesController {
    @GetMapping("/calificaciones")
    public String index(Model model) {
        return "calificaciones/index";
    }
}

