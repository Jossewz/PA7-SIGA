package com.siga.siga_iea.storage;

import com.siga.siga_iea.storage.exception.StorageFileNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.InputStream;
import java.net.URLConnection;

/**
 * Controller that serves public files and resource previews.
 * <p>
 * Protected files are served by domain controllers after validating permissions.
 * </p>
 */
@Controller
@RequestMapping("/storage/public")
public class StorageController {

    private static final Logger log = LoggerFactory.getLogger(StorageController.class);

    private final StorageService storageService;

    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/view")
    public ResponseEntity<InputStreamResource> viewFile(@RequestParam(value = "key", required = false) String key) {
        if (key == null || key.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            InputStream stream = storageService.download(key);
            String contentType = URLConnection.guessContentTypeFromName(key);
            if (contentType == null) {
                String lower = key.toLowerCase();
                if (lower.endsWith(".png")) contentType = "image/png";
                else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) contentType = "image/jpeg";
                else if (lower.endsWith(".pdf")) contentType = "application/pdf";
                else contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .body(new InputStreamResource(stream));
        } catch (StorageFileNotFoundException e) {
            log.warn("Archivo no encontrado con key: {}", key);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error al descargar archivo con key '{}': {}", key, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Serves a public file from the "institucion/" folder of the bucket.
     * Example: GET /storage/public/logo.png -> key "institucion/logo.png"
     */
    @GetMapping("/{*path}")
    public ResponseEntity<InputStreamResource> servePublicFile(@PathVariable(value = "path", required = false) String path) {
        if (path == null || path.isBlank() || path.equals("/") || path.equals("/view")) {
            return ResponseEntity.badRequest().build();
        }

        String cleanPath = path.startsWith("/") ? path.substring(1) : path;
        String key = StorageFolder.INSTITUCION.getPath() + "/" + cleanPath;

        try {
            InputStream stream = storageService.download(key);

            String contentType = URLConnection.guessContentTypeFromName(cleanPath);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(getFilename(cleanPath)).build().toString())
                    .body(new InputStreamResource(stream));

        } catch (StorageFileNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error al servir archivo institucional '{}': {}", cleanPath, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String getFilename(String path) {
        int lastSlash = path.lastIndexOf('/');
        return (lastSlash >= 0) ? path.substring(lastSlash + 1) : path;
    }
}
