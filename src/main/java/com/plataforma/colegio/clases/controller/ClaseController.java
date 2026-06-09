package com.plataforma.colegio.clases.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ClaseController {
    @GetMapping("/clases")
    public String index(Model model) {
        return "clases/index";
    }
}
