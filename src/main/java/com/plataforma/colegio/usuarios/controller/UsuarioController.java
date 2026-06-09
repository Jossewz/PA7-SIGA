package com.plataforma.colegio.usuarios.controller;

import com.plataforma.colegio.usuarios.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping("/usuarios")
    public String list(Model model) {
        model.addAttribute("usuarios", service.findAll());
        return "usuarios/index";
    }
}
