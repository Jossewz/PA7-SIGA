package com.plataforma.colegio.auth.controller;

import com.plataforma.colegio.auth.dto.LoginRequestDTO;
import com.plataforma.colegio.auth.dto.LoginResponseDTO;
import com.plataforma.colegio.auth.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/auth/login")
    public String loginForm(Model model) {
        model.addAttribute("loginRequest", new LoginRequestDTO());
        return "auth/index";
    }

    @PostMapping("/auth/login")
    public String login(@ModelAttribute LoginRequestDTO loginRequest, Model model) {
        LoginResponseDTO response = authService.authenticate(loginRequest);
        model.addAttribute("response", response);
        return "redirect:/dashboard";
    }
}
