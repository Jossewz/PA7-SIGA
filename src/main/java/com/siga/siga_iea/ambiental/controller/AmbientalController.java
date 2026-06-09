package com.siga.siga_iea.ambiental.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AmbientalController {
    @GetMapping("/ambiental")
    public String index(Model model) {
        return "ambiental/index";
    }
}

