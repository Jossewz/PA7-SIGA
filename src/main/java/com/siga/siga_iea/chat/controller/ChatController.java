package com.siga.siga_iea.chat.controller;

import com.siga.siga_iea.auth.service.CurrentUserContextService;
import com.siga.siga_iea.chat.dto.ChatMessageRequest;
import com.siga.siga_iea.chat.dto.ChatMessageResponse;
import com.siga.siga_iea.chat.service.ChatOrchestratorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/chat")
public class ChatController {

    private final ChatOrchestratorService orchestratorService;
    private final CurrentUserContextService userContextService;

    public ChatController(ChatOrchestratorService orchestratorService, 
                          CurrentUserContextService userContextService) {
        this.orchestratorService = orchestratorService;
        this.userContextService = userContextService;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("rol", userContextService.getRolAutenticado().name());
        model.addAttribute("usuarioNombre", userContextService.getUsuarioAutenticado()
                .map(u -> u.getEmail())
                .orElse("Usuario"));
        return "chat/index";
    }

    @PostMapping("/mensaje")
    public String enviarMensaje(@RequestParam("mensaje") String mensaje, Model model) {
        ChatMessageRequest request = new ChatMessageRequest(mensaje, null);
        ChatMessageResponse response = orchestratorService.procesarMensaje(request);

        model.addAttribute("mensajeUsuario", mensaje);
        model.addAttribute("respuestaBot", response);
        return "chat/fragments :: turnoConversacion";
    }

    @PostMapping(value = "/api/mensaje", produces = "application/json")
    @ResponseBody
    public ChatMessageResponse apiMensaje(@RequestBody ChatMessageRequest request) {
        return orchestratorService.procesarMensaje(request);
    }
}
