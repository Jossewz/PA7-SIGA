package com.plataforma.colegio.matricula.htmx;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MatriculaHtmxController {

    @GetMapping("/matricula/fragment")
    public String fragment() {
        return "matricula/fragments/fragment";
    }
}
