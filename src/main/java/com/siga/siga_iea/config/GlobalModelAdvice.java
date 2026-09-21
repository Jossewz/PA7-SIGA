package com.siga.siga_iea.config;

import com.siga.siga_iea.auth.enums.RolEnum;
import com.siga.siga_iea.auth.service.CurrentUserContextService;
import com.siga.siga_iea.configuracion.service.RolPermisoService;
import com.siga.siga_iea.usuarios.entity.Docente;
import com.siga.siga_iea.usuarios.entity.Usuario;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collections;
import java.util.Set;

@ControllerAdvice
public class GlobalModelAdvice {

    private final CurrentUserContextService userContextService;
    private final RolPermisoService rolPermisoService;

    public GlobalModelAdvice(CurrentUserContextService userContextService,
                             RolPermisoService rolPermisoService) {
        this.userContextService = userContextService;
        this.rolPermisoService = rolPermisoService;
    }

    @ModelAttribute("modulosPermitidos")
    public Set<String> populateModulosPermitidos() {
        if (userContextService.getAuthentication().isPresent()) {
            RolEnum rol = userContextService.getRolAutenticado();
            return rolPermisoService.obtenerModulosPermitidos(rol.name());
        }
        return Collections.emptySet();
    }

    @ModelAttribute("currentUser")
    public Usuario populateCurrentUser() {
        return userContextService.getUsuarioAutenticado().orElse(null);
    }

    @ModelAttribute("currentRoleName")
    public String populateRoleName() {
        if (userContextService.getAuthentication().isPresent()) {
            return userContextService.getRolAutenticado().getEtiqueta();
        }
        return "";
    }

    @ModelAttribute("esAdmin")
    public boolean populateEsAdmin() {
        return userContextService.esAdmin();
    }

    @ModelAttribute("esDocente")
    public boolean populateEsDocente() {
        return userContextService.esDocente();
    }

    @ModelAttribute("esAdministrativo")
    public boolean populateEsAdministrativo() {
        return userContextService.esPersonalAdministrativo();
    }

    @ModelAttribute("puedeAdministrarCursos")
    public boolean populatePuedeAdministrarCursos() {
        return userContextService.esAdminOAdministrativo();
    }

    @ModelAttribute("docenteLogueado")
    public Docente populateDocenteLogueado() {
        return userContextService.getDocenteAutenticado().orElse(null);
    }
}
