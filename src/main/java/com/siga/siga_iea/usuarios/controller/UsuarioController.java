package com.siga.siga_iea.usuarios.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UsuarioController {

    @GetMapping("/usuarios")
    public String index() {
        return "redirect:/estudiantes";
    }
}
