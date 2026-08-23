package com.siga.siga_iea.storage;

import com.siga.siga_iea.storage.exception.StorageFileNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.InputStream;
import java.net.URLConnection;

/**
 * Controller that serves ONLY public files (logos, banners, institutional images).
 * <p>
 * Protected files (student photos, documents, etc.) are served by their respective
 * domain controllers, which validate permissions before delegating to {@link StorageService}.
 * </p>
 */
@Controller
@RequestMapping("/storage/public")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    @GetMapping("/view")
    public ResponseEntity<InputStreamResource> viewFile(@org.springframework.web.bind.annotation.RequestParam("key") String key) {
        try {
            InputStream stream = storageService.download(key);
            String contentType = URLConnection.guessContentTypeFromName(key);
            if (contentType == null) {
                String lower = key.toLowerCase();
                if (lower.endsWith(".png")) contentType = "image/png";
                else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) contentType = "image/jpeg";
                else if (lower.endsWith(".pdf")) contentType = "application/pdf";
                else contentType = "image/jpeg";
            }
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .body(new InputStreamResource(stream));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Serves a public file from the "institucion/" folder of the bucket.
     * The path after /storage/public/ maps to the object key under "institucion/".
     * <p>
     * Example: GET /storage/public/logo.png → key "institucion/logo.png"
     * </p>
     */
    @GetMapping("/**")
    public ResponseEntity<InputStreamResource> servePublicFile(HttpServletRequest request) {
        // Extract the path after "/storage/public/"
        String path = request.getRequestURI().substring("/storage/public/".length());

        if (path.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // All public files live under the "institucion/" folder
        String key = StorageFolder.INSTITUCION.getPath() + "/" + path;

        try {
            InputStream stream = storageService.download(key);

            // Guess content type from the filename
            String contentType = URLConnection.guessContentTypeFromName(path);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + getFilename(path) + "\"")
                    .body(new InputStreamResource(stream));

        } catch (StorageFileNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private String getFilename(String path) {
        int lastSlash = path.lastIndexOf('/');
        return (lastSlash >= 0) ? path.substring(lastSlash + 1) : path;
    }
}
