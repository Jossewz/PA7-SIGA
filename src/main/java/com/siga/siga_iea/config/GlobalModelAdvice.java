package com.siga.siga_iea.config;

import com.siga.siga_iea.auth.security.CustomUserDetailsService;
import com.siga.siga_iea.usuarios.entity.Usuario;
import com.siga.siga_iea.usuarios.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@ControllerAdvice
public class GlobalModelAdvice {

    private final UsuarioRepository usuarioRepository;

    public GlobalModelAdvice(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @ModelAttribute("currentUser")
    public Usuario populateCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String email = auth.getName();
            Optional<Usuario> userOpt = usuarioRepository.findByEmail(email);
            return userOpt.orElse(null);
        }
        return null;
    }

    @ModelAttribute("currentRoleName")
    public String populateRoleName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String email = auth.getName();
            Optional<Usuario> userOpt = usuarioRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                String role = CustomUserDetailsService.normalizeRole(userOpt.get().getRol());
                return switch (role) {
                    case "ADMIN" -> "Administrador";
                    case "PERSONAL_ADMINISTRATIVO" -> "Personal Administrativo";
                    case "DOCENTE" -> "Docente";
                    case "ESTUDIANTE" -> "Estudiante";
                    default -> role;
                };
            }
        }
        return "";
    }
}
