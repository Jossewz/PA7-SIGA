package com.siga.siga_iea.storage.service;

import com.siga.siga_iea.matricula.entity.Matricula;
import com.siga.siga_iea.storage.StorageFolder;
import com.siga.siga_iea.storage.StorageService;
import com.siga.siga_iea.storage.dto.UploadResult;
import com.siga.siga_iea.storage.entity.Documento;
import com.siga.siga_iea.storage.entity.TipoDocumento;
import com.siga.siga_iea.storage.exception.StorageException;
import com.siga.siga_iea.storage.repository.DocumentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentoService {

    private final StorageService storageService;
    private final DocumentoRepository documentoRepository;

    /**
     * Sube un archivo temporal a MinIO (antes de finalizar matrícula).
     * Retorna el storageKey para guardarlo en sesión.
     */
    public String subirTemporal(MultipartFile file, String sessionId, String... allowedExtensions) {
        storageService.validateSize(file, 10 * 1024 * 1024); // 10MB max
        storageService.validateExtension(file, allowedExtensions);
        UploadResult result = storageService.upload(file, StorageFolder.TEMP, sessionId);
        return result.getKey();
    }

    /**
     * Elimina un archivo temporal de MinIO (usuario cancela un documento).
     */
    public void eliminarTemporal(String storageKey) {
        if (storageService.exists(storageKey)) {
            storageService.delete(storageKey);
        }
    }

    /**
     * Vincula un archivo temporal a una matrícula:
     * 1. Mueve de temp/ a matriculas/{id}/
     * 2. Calcula checksum SHA-256
     * 3. Crea registro Documento en BD
     */
    @Transactional
    public Documento vincularAMatricula(String tempKey, Matricula matricula, TipoDocumento tipo,
                                         String nombreOriginal, String contentType, Long size) {
        // Mover archivo de temp/ -> matriculas/{matriculaId}/
        String filename = tempKey.substring(tempKey.lastIndexOf('/') + 1);
        String newKey = StorageFolder.MATRICULAS.getPath() + "/" + matricula.getId() + "/" + filename;
        storageService.move(tempKey, newKey);

        // Calcular checksum
        String checksum = calcularChecksum(newKey);

        // Persistir en BD
        Documento doc = new Documento();
        doc.setStorageKey(newKey);
        doc.setNombreOriginal(nombreOriginal);
        doc.setContentType(contentType);
        doc.setSize(size);
        doc.setTipoDocumento(tipo);
        doc.setChecksum(checksum);
        doc.setMatricula(matricula);
        doc.setFechaCreacion(LocalDateTime.now());

        return documentoRepository.save(doc);
    }

    /**
     * Elimina un documento: borra de MinIO y de BD.
     */
    @Transactional
    public void eliminar(UUID documentoId) {
        Documento doc = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new StorageException("Documento no encontrado: " + documentoId));
        storageService.delete(doc.getStorageKey());
        documentoRepository.delete(doc);
    }

    /**
     * Obtiene URL temporal para visualizar en el navegador.
     */
    public String obtenerUrlTemporal(UUID documentoId, int minutosExpiracion) {
        Documento doc = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new StorageException("Documento no encontrado: " + documentoId));
        return storageService.generatePresignedUrl(doc.getStorageKey(), minutosExpiracion);
    }

    /**
     * Obtiene URL temporal usando la clave de almacenamiento (para archivos temporales).
     */
    public String obtenerUrlTemporalPorKey(String key, int minutosExpiracion) {
        return storageService.generatePresignedUrl(key, minutosExpiracion);
    }

    /**
     * Lista documentos de una matrícula.
     */
    public List<Documento> listarPorMatricula(UUID matriculaId) {
        return documentoRepository.findByMatriculaId(matriculaId);
    }

    /**
     * Calcula SHA-256 del archivo almacenado.
     */
    private String calcularChecksum(String key) {
        try (InputStream is = storageService.download(key)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (Exception e) {
            log.warn("No se pudo calcular checksum para '{}': {}", key, e.getMessage());
            return null;
        }
    }
}
