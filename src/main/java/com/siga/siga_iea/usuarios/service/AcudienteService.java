package com.siga.siga_iea.usuarios.service;

import com.siga.siga_iea.usuarios.entity.Acudiente;
import com.siga.siga_iea.usuarios.repository.AcudienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AcudienteService {

    private final AcudienteRepository acudienteRepository;

    public AcudienteService(AcudienteRepository acudienteRepository) {
        this.acudienteRepository = acudienteRepository;
    }

    @Transactional
    public Acudiente buscarOCrear(String nombres, String apellidos, String parentesco,
                                  String tipoDoc, String numDoc, String telefono, String direccion) {
        if (numDoc != null && !numDoc.isBlank()) {
            Optional<Acudiente> existente = acudienteRepository.findByNumeroDocumento(numDoc.trim());
            if (existente.isPresent()) {
                Acudiente ac = existente.get();
                if (nombres != null && !nombres.isBlank()) ac.setNombres(nombres.trim());
                if (apellidos != null && !apellidos.isBlank()) ac.setApellidos(apellidos.trim());
                if (parentesco != null && !parentesco.isBlank()) ac.setParentesco(parentesco.trim());
                if (telefono != null && !telefono.isBlank()) ac.setTelefono(telefono.trim());
                if (direccion != null && !direccion.isBlank()) ac.setDireccion(direccion.trim());
                return acudienteRepository.save(ac);
            }
        }

        Acudiente nuevo = new Acudiente();
        nuevo.setNombres(nombres != null ? nombres.trim() : "Sin Nombre");
        nuevo.setApellidos(apellidos != null ? apellidos.trim() : "");
        nuevo.setParentesco(parentesco != null ? parentesco.trim() : "Acudiente");
        nuevo.setTipoDocumento(tipoDoc != null ? tipoDoc.trim() : "CC");
        nuevo.setNumeroDocumento(numDoc != null ? numDoc.trim() : null);
        nuevo.setTelefono(telefono != null ? telefono.trim() : "");
        nuevo.setDireccion(direccion != null ? direccion.trim() : "");

        return acudienteRepository.save(nuevo);
    }
}
