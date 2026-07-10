package com.siga.siga_iea.usuarios.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class UsuarioController {

    @GetMapping("/usuarios")
    public String index(Model model) {
        model.addAttribute("title", "Usuarios del Sistema – IEACI");
        model.addAttribute("activePage", "usuarios");

        // Mock list of users
        List<Map<String, Object>> usuariosList = new ArrayList<>();
        usuariosList.add(createUser("c.mendoza", "Carlos Mendoza", "DOCENTE", "Ayer, 04:32 PM", "Activo"));
        usuariosList.add(createUser("a.sanchez", "Ana María Sánchez", "COORDINADOR", "Hoy, 08:15 AM", "Activo"));
        usuariosList.add(createUser("l.gomez", "Luis Felipe Gómez", "SECRETARÍA", "Hace 2 horas", "Activo"));
        usuariosList.add(createUser("p.lopez", "Patricia López", "DOCENTE", "Hace 5 días", "Bloqueado"));
        usuariosList.add(createUser("admin.siga", "Jorge Eliécer Rojas", "ADMINISTRADOR", "Hoy, 10:30 AM", "Activo"));

        model.addAttribute("usuariosList", usuariosList);
        return "usuarios/index";
    }

    private Map<String, Object> createUser(String username, String persona, String rol, String ultimoAcceso, String estado) {
        Map<String, Object> u = new HashMap<>();
        u.put("username", username);
        u.put("persona", persona);
        u.put("rol", rol);
        u.put("ultimoAcceso", ultimoAcceso);
        u.put("estado", estado);
        return u;
    }
}
