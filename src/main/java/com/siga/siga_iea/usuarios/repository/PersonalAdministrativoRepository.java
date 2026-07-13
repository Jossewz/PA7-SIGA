package com.siga.siga_iea.usuarios.repository;

import com.siga.siga_iea.usuarios.entity.PersonalAdministrativo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface PersonalAdministrativoRepository extends JpaRepository<PersonalAdministrativo, UUID> {
}
