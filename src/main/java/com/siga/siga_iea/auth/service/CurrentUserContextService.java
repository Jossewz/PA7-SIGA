package com.siga.siga_iea.auth.service;

import com.siga.siga_iea.auth.enums.RolEnum;
import com.siga.siga_iea.usuarios.entity.Docente;
import com.siga.siga_iea.usuarios.entity.Estudiante;
import com.siga.siga_iea.usuarios.entity.Usuario;
import com.siga.siga_iea.usuarios.repository.DocenteRepository;
import com.siga.siga_iea.usuarios.repository.EstudianteRepository;
import com.siga.siga_iea.usuarios.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Servicio centralizado para resolución limpia y tipada del contexto del usuario autenticado.
 * Desacopla controladores y servicios de la inyección directa de repositorios de usuario y docente.
 */
@Service
public class CurrentUserContextService {

    private final UsuarioRepository usuarioRepository;
    private final DocenteRepository docenteRepository;
    private final EstudianteRepository estudianteRepository;

    public CurrentUserContextService(UsuarioRepository usuarioRepository, 
                                     DocenteRepository docenteRepository,
                                     EstudianteRepository estudianteRepository) {
        this.usuarioRepository = usuarioRepository;
        this.docenteRepository = docenteRepository;
        this.estudianteRepository = estudianteRepository;
    }

    public Optional<Authentication> getAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }
        return Optional.of(auth);
    }

    public Optional<Usuario> getUsuarioAutenticado() {
        return getAuthentication()
                .map(Authentication::getName)
                .flatMap(usuarioRepository::findByEmail);
    }

    public RolEnum getRolAutenticado() {
        return getUsuarioAutenticado()
                .map(Usuario::getRolEnum)
                .orElse(RolEnum.ESTUDIANTE);
    }

    public boolean esAdmin() {
        return getRolAutenticado().esAdmin();
    }

    public boolean esDocente() {
        return getRolAutenticado().esDocente();
    }

    public boolean esEstudiante() {
        return getRolAutenticado().esEstudiante();
    }

    public boolean esPersonalAdministrativo() {
        return getRolAutenticado().esPersonalAdministrativo();
    }

    public boolean esAdminOAdministrativo() {
        return getRolAutenticado().esAdminOAdministrativo();
    }

    public Optional<Docente> getDocenteAutenticado() {
        return getUsuarioAutenticado()
                .filter(u -> u.getRolEnum().esDocente())
                .map(Usuario::getNumeroDocumento)
                .filter(doc -> doc != null && !doc.isBlank())
                .flatMap(doc -> docenteRepository.findByNumeroDocumento(doc.trim()));
    }

    public Optional<Estudiante> getEstudianteAutenticado() {
        return getUsuarioAutenticado()
                .filter(u -> u.getRolEnum().esEstudiante())
                .map(Usuario::getNumeroDocumento)
                .filter(doc -> doc != null && !doc.isBlank())
                .flatMap(doc -> estudianteRepository.findByNumeroDocumento(doc.trim()));
    }
}
