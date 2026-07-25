package com.siga.siga_iea.calificaciones.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CalificacionController {

    @GetMapping("/calificaciones")
    public String index(Model model) {
        model.addAttribute("title", "Calificaciones – IEACI");
        model.addAttribute("activePage", "calificaciones");
        return "calificaciones/index";
    }
}
