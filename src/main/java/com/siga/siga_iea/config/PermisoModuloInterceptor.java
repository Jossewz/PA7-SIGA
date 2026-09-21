package com.siga.siga_iea.config;

import com.siga.siga_iea.auth.security.CustomUserDetailsService;
import com.siga.siga_iea.configuracion.service.RolPermisoService;
import com.siga.siga_iea.usuarios.entity.Usuario;
import com.siga.siga_iea.usuarios.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class PermisoModuloInterceptor implements HandlerInterceptor {

    private final RolPermisoService rolPermisoService;
    private final UsuarioRepository usuarioRepository;

    public PermisoModuloInterceptor(RolPermisoService rolPermisoService, UsuarioRepository usuarioRepository) {
        this.rolPermisoService = rolPermisoService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        String moduloRequerido = obtenerModuloParaUri(uri);
        if (moduloRequerido == null) {
            return true;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return true;
        }

        String email = auth.getName();
        Optional<Usuario> userOpt = usuarioRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return true;
        }

        com.siga.siga_iea.auth.enums.RolEnum rolEnum = userOpt.get().getRolEnum();
        if (rolEnum.esAdmin()) {
            return true;
        }

        // Sub-ruta sensible: Solo ADMIN puede ver o modificar roles y permisos
        if (uri.startsWith("/configuracion/roles")) {
            String isHx = request.getHeader("HX-Request");
            if (isHx != null && !isHx.isBlank()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return false;
            }
            response.sendRedirect(request.getContextPath() + "/?sinAcceso=ROLES_PERMISOS");
            return false;
        }

        // Sub-ruta sensible: Mapear y promover estudiantes solo ADMIN y PERSONAL_ADMINISTRATIVO
        if ((uri.startsWith("/clases/mapear-estudiantes") || uri.startsWith("/clases/promover-estudiantes"))
                && !rolEnum.esPersonalAdministrativo()) {
            String isHx = request.getHeader("HX-Request");
            if (isHx != null && !isHx.isBlank()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return false;
            }
            response.sendRedirect(request.getContextPath() + "/?sinAcceso=CURSOS_HORARIOS");
            return false;
        }

        boolean tieneAcceso = rolPermisoService.tieneAcceso(rolEnum.name(), moduloRequerido);
        if (!tieneAcceso) {
            String isHx = request.getHeader("HX-Request");
            if (isHx != null && !isHx.isBlank()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return false;
            }
            response.sendRedirect(request.getContextPath() + "/?sinAcceso=" + moduloRequerido);
            return false;
        }

        return true;
    }

    private String obtenerModuloParaUri(String uri) {
        if (uri.startsWith("/matricula")) return "MATRICULAS";
        if (uri.startsWith("/personal")) return "PERSONAL";
        if (uri.startsWith("/estudiantes")) return "MATRICULAS";
        if (uri.startsWith("/clases")) return "CURSOS_HORARIOS";
        if (uri.startsWith("/calificaciones")) return "CALIFICACIONES";
        if (uri.startsWith("/asistencias")) return "ASISTENCIAS";
        if (uri.startsWith("/certificados")) return "CERTIFICADOS";
        if (uri.startsWith("/reportes")) return "REPORTES";
        if (uri.startsWith("/configuracion")) return "CONFIGURACION";
        if (uri.startsWith("/ambiental")) return "MODULO_AMBIENTAL";
        return null;
    }
}
