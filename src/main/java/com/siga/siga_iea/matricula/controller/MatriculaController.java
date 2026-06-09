package com.siga.siga_iea.matricula.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MatriculaController {
    @GetMapping("/matricula")
    public String index(Model model) {
        return "matricula/index";
    }
}

