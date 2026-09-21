package com.siga.siga_iea.configuracion.repository;

import com.siga.siga_iea.configuracion.entity.RolPermiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RolPermisoRepository extends JpaRepository<RolPermiso, UUID> {
    List<RolPermiso> findByRolOrderByModuloAsc(String rol);
    Optional<RolPermiso> findByRolAndModulo(String rol, String modulo);
}
