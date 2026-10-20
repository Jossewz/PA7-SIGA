package com.siga.siga_iea.certificados.service;

import com.siga.siga_iea.certificados.entity.SolicitudCertificado;
import com.siga.siga_iea.certificados.repository.SolicitudCertificadoRepository;
import com.siga.siga_iea.storage.StorageFolder;
import com.siga.siga_iea.storage.StorageService;
import com.siga.siga_iea.storage.dto.UploadResult;
import com.siga.siga_iea.usuarios.entity.Estudiante;
import com.siga.siga_iea.usuarios.entity.PersonalAdministrativo;
import com.siga.siga_iea.usuarios.repository.EstudianteRepository;
import com.siga.siga_iea.usuarios.repository.PersonalAdministrativoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
public class CertificadoService {

    private final SolicitudCertificadoRepository solicitudRepository;
    private final EstudianteRepository estudianteRepository;
    private final PersonalAdministrativoRepository personalRepository;
    private final StorageService storageService;
    private final com.siga.siga_iea.configuracion.service.PeriodoConfigService periodoConfigService;

    public CertificadoService(SolicitudCertificadoRepository solicitudRepository,
                              EstudianteRepository estudianteRepository,
                              PersonalAdministrativoRepository personalRepository,
                              StorageService storageService,
                              @org.springframework.beans.factory.annotation.Autowired(required = false)
                              com.siga.siga_iea.configuracion.service.PeriodoConfigService periodoConfigService) {
        this.solicitudRepository = solicitudRepository;
        this.estudianteRepository = estudianteRepository;
        this.personalRepository = personalRepository;
        this.storageService = storageService;
        this.periodoConfigService = periodoConfigService;
    }

    public List<SolicitudCertificado> listarTodas() {
        return solicitudRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<SolicitudCertificado> listarPorEstudiante(UUID estudianteId) {
        return solicitudRepository.findByEstudianteIdOrderByCreatedAtDesc(estudianteId);
    }

    public List<SolicitudCertificado> filtrarPorEstado(String estado) {
        if (estado == null || estado.isBlank() || "todos".equalsIgnoreCase(estado)) {
            return listarTodas();
        }
        return solicitudRepository.findByEstado(estado);
    }

    public Optional<SolicitudCertificado> buscarPorId(UUID id) {
        return solicitudRepository.findById(id);
    }

    public Optional<SolicitudCertificado> buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) return Optional.empty();
        return solicitudRepository.findByCodigo(codigo.trim());
    }

    @Transactional
    public SolicitudCertificado crearSolicitud(UUID estudianteId, String tipo, String categoria, String motivo, String anoLectivo, String gradoReferencia) {
        Estudiante est = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));

        String anio = (anoLectivo != null && !anoLectivo.isBlank()) ? anoLectivo.trim() :
                (periodoConfigService != null ? String.valueOf(periodoConfigService.obtenerAnioLectivoActual().getAnio()) : "2026");

        long consecutivo = solicitudRepository.count() + 1;
        SolicitudCertificado s = new SolicitudCertificado();
        s.setCodigo(String.format("CERT-%s-%04d", anio, consecutivo));
        s.setEstudiante(est);
        s.setTipo(tipo);
        s.setCategoria(categoria != null ? categoria : "Constancias Administrativas");
        s.setMotivo(motivo);
        s.setAnoLectivo(anio);
        s.setGradoReferencia(gradoReferencia != null ? gradoReferencia : "11°");
        s.setEstado("Pendiente");

        return solicitudRepository.save(s);
    }

    @Transactional
    public SolicitudCertificado responderSolicitud(UUID solicitudId, String mensajeRespuesta, MultipartFile archivoPDF, UUID adminId) {
        SolicitudCertificado s = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));

        s.setEstado("Resuelto");
        s.setMensajeRespuesta(mensajeRespuesta);

        if (adminId != null) {
            personalRepository.findById(adminId).ifPresent(s::setRespondidoPor);
        }

        if (archivoPDF != null && !archivoPDF.isEmpty()) {
            storageService.validateExtension(archivoPDF, "pdf");
            UploadResult result = storageService.upload(archivoPDF, StorageFolder.ESTUDIANTES, s.getCodigo());
            s.setArchivoAdjuntoKey(result.getKey());
        }

        return solicitudRepository.save(s);
    }
}
