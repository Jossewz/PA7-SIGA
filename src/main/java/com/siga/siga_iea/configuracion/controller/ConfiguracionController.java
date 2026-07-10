package com.siga.siga_iea.configuracion.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ConfiguracionController {

    @GetMapping("/configuracion")
    public String index(Model model) {
        model.addAttribute("title", "Configuración General – IEACI");
        model.addAttribute("activePage", "configuracion");
        
        // Institutional data
        model.addAttribute("nit", "800.124.567-2");
        model.addAttribute("nombreInst", "Institución Educativa Ateneo de la Ciencia e Innovación (IEACI)");
        model.addAttribute("direccionInst", "San José de los Campanos, Mz 32 Lote 9-11");
        model.addAttribute("telefonoInst", "300 987 6543");
        model.addAttribute("correoInst", "contacto@ieaci.edu.co");
        model.addAttribute("añoLectivo", "2026");

        return "configuracion/index";
    }
}
