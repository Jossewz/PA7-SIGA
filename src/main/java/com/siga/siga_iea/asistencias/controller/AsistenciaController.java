package com.siga.siga_iea.asistencias.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AsistenciaController {

    @GetMapping("/asistencias")
    public String index(Model model) {
        model.addAttribute("title", "Asistencias – IEACI");
        model.addAttribute("activePage", "asistencias");
        return "asistencias/index";
    }
}
