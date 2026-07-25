package com.siga.siga_iea.shared.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SoporteController {

    @GetMapping("/soporte")
    public String index(Model model) {
        model.addAttribute("title", "Soporte Técnico – IEACI");
        model.addAttribute("activePage", "soporte");
        return "soporte/index";
    }
}
