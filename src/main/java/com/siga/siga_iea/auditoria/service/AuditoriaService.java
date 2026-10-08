package com.siga.siga_iea.auditoria.service;

import com.siga.siga_iea.auditoria.entity.AuditoriaCambio;
import com.siga.siga_iea.auditoria.repository.AuditoriaCambioRepository;
import com.siga.siga_iea.auth.service.CurrentUserContextService;
import com.siga.siga_iea.usuarios.entity.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuditoriaService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);

    private final AuditoriaCambioRepository auditoriaCambioRepository;
    private final CurrentUserContextService currentUserContextService;

    public AuditoriaService(AuditoriaCambioRepository auditoriaCambioRepository,
                            CurrentUserContextService currentUserContextService) {
        this.auditoriaCambioRepository = auditoriaCambioRepository;
        this.currentUserContextService = currentUserContextService;
    }

    /**
     * Registra un cambio en la tabla de auditoría.
     * Participa en la misma transacción del cambio de negocio (Propagation.REQUIRED)
     * para asegurar que si la operación principal falla o hace rollback, no queden registros huérfanos.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public AuditoriaCambio registrarCambio(String entidad, UUID entidadId, String campo,
                                          String valorAnterior, String valorNuevo) {
        UUID usuarioId = null;
        String usuarioEmail = null;

        if (currentUserContextService != null) {
            try {
                Optional<Usuario> userOpt = currentUserContextService.getUsuarioAutenticado();
                if (userOpt.isPresent()) {
                    usuarioId = userOpt.get().getId();
                    usuarioEmail = userOpt.get().getEmail();
                } else {
                    usuarioEmail = currentUserContextService.getAuthentication()
                            .map(org.springframework.security.core.Authentication::getName)
                            .orElse("SISTEMA");
                }
            } catch (Exception e) {
                usuarioEmail = "SISTEMA";
            }
        }

        AuditoriaCambio cambio = new AuditoriaCambio(entidad, entidadId, campo, valorAnterior, valorNuevo, usuarioId, usuarioEmail);
        AuditoriaCambio guardado = auditoriaCambioRepository.save(cambio);
        log.info("Auditoría registrada: entidad={}, id={}, campo={}, '{}' -> '{}', usuario={}",
                entidad, entidadId, campo, valorAnterior, valorNuevo, usuarioEmail);
        return guardado;
    }

    @Transactional(readOnly = true)
    public List<AuditoriaCambio> consultarHistorial(String entidad, UUID entidadId) {
        return auditoriaCambioRepository.findByEntidadAndEntidadIdOrderByFechaDesc(entidad, entidadId);
    }
}
