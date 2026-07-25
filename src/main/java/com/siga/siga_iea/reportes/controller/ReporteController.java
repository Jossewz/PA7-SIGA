package com.siga.siga_iea.reportes.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReporteController {

    @GetMapping("/reportes")
    public String index(Model model) {
        model.addAttribute("title", "Reportes – IEACI");
        model.addAttribute("activePage", "reportes");
        return "reportes/index";
    }
}
