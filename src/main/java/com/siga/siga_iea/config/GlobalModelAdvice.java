package com.siga.siga_iea.config;

import com.siga.siga_iea.auth.security.CustomUserDetailsService;
import com.siga.siga_iea.usuarios.entity.Docente;
import com.siga.siga_iea.usuarios.entity.Usuario;
import com.siga.siga_iea.usuarios.repository.DocenteRepository;
import com.siga.siga_iea.usuarios.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.siga.siga_iea.configuracion.service.RolPermisoService;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@ControllerAdvice
public class GlobalModelAdvice {

    private final UsuarioRepository usuarioRepository;
    private final DocenteRepository docenteRepository;
    private final RolPermisoService rolPermisoService;

    public GlobalModelAdvice(UsuarioRepository usuarioRepository,
                             DocenteRepository docenteRepository,
                             RolPermisoService rolPermisoService) {
        this.usuarioRepository = usuarioRepository;
        this.docenteRepository = docenteRepository;
        this.rolPermisoService = rolPermisoService;
    }

    @ModelAttribute("modulosPermitidos")
    public Set<String> populateModulosPermitidos() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String email = auth.getName();
            Optional<Usuario> userOpt = usuarioRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                String role = CustomUserDetailsService.normalizeRole(userOpt.get().getRol());
                return rolPermisoService.obtenerModulosPermitidos(role);
            }
        }
        return Collections.emptySet();
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

    @ModelAttribute("esAdmin")
    public boolean populateEsAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String email = auth.getName();
            Optional<Usuario> userOpt = usuarioRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                String role = CustomUserDetailsService.normalizeRole(userOpt.get().getRol());
                return "ADMIN".equalsIgnoreCase(role);
            }
        }
        return false;
    }

    @ModelAttribute("esDocente")
    public boolean populateEsDocente() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String email = auth.getName();
            Optional<Usuario> userOpt = usuarioRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                String role = CustomUserDetailsService.normalizeRole(userOpt.get().getRol());
                return "DOCENTE".equalsIgnoreCase(role);
            }
        }
        return false;
    }

    @ModelAttribute("esAdministrativo")
    public boolean populateEsAdministrativo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String email = auth.getName();
            Optional<Usuario> userOpt = usuarioRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                String role = CustomUserDetailsService.normalizeRole(userOpt.get().getRol());
                return "PERSONAL_ADMINISTRATIVO".equalsIgnoreCase(role);
            }
        }
        return false;
    }

    @ModelAttribute("puedeAdministrarCursos")
    public boolean populatePuedeAdministrarCursos() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String email = auth.getName();
            Optional<Usuario> userOpt = usuarioRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                String role = CustomUserDetailsService.normalizeRole(userOpt.get().getRol());
                return "ADMIN".equalsIgnoreCase(role) || "PERSONAL_ADMINISTRATIVO".equalsIgnoreCase(role);
            }
        }
        return false;
    }

    @ModelAttribute("docenteLogueado")
    public Docente populateDocenteLogueado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String email = auth.getName();
            Optional<Usuario> userOpt = usuarioRepository.findByEmail(email);
            if (userOpt.isPresent() && "DOCENTE".equalsIgnoreCase(CustomUserDetailsService.normalizeRole(userOpt.get().getRol()))) {
                String doc = userOpt.get().getNumeroDocumento();
                if (doc != null && !doc.isBlank()) {
                    return docenteRepository.findByNumeroDocumento(doc.trim()).orElse(null);
                }
            }
        }
        return null;
    }
}
