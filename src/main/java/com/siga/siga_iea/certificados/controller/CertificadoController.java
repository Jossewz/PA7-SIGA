package com.siga.siga_iea.certificados.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CertificadoController {

    @GetMapping("/certificados")
    public String index(Model model) {
        model.addAttribute("title", "Certificados – IEACI");
        model.addAttribute("activePage", "certificados");
        return "certificados/index";
    }
}
