package com.plataforma.colegio.reportes.controller;

import com.plataforma.colegio.reportes.service.ReportesService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReportesController {

    private final ReportesService service;

    public ReportesController(ReportesService service) {
        this.service = service;
    }

    @GetMapping("/reportes")
    public String index(Model model) {
        model.addAttribute("reportes", service.getReportes());
        return "reportes/index";
    }
}
