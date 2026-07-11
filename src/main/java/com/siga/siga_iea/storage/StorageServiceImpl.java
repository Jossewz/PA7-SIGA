package com.siga.siga_iea.storage;

import com.siga.siga_iea.storage.dto.FileMetadata;
import com.siga.siga_iea.storage.dto.UploadResult;
import com.siga.siga_iea.storage.exception.StorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of {@link StorageService} that delegates
 * all low-level operations to a {@link StorageProvider}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {

    private final StorageProvider provider;
    private final StorageProperties properties;

    // ====================== Upload ======================

    @Override
    public UploadResult upload(MultipartFile file, StorageFolder folder, String entityId) {
        if (file == null || file.isEmpty()) {
            throw new StorageException("Cannot upload an empty file.");
        }

        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "file"
        );
        String extension = getExtension(originalFilename);
        String uuidFilename = UUID.randomUUID().toString() + extension;
        String key = buildKey(folder, entityId, uuidFilename);

        try (InputStream stream = file.getInputStream()) {
            provider.putObject(key, stream, file.getSize(), file.getContentType());
        } catch (IOException e) {
            throw new StorageException("Failed to read uploaded file: " + originalFilename, e);
        }

        FileMetadata metadata = FileMetadata.builder()
                .key(key)
                .originalFilename(originalFilename)
                .contentType(file.getContentType())
                .size(file.getSize())
                .bucket(properties.getBucketName())
                .uploadDate(LocalDateTime.now())
                .build();

        log.info("Uploaded file '{}' as key '{}'", originalFilename, key);

        return UploadResult.builder()
                .key(key)
                .metadata(metadata)
                .build();
    }

    @Override
    public UploadResult upload(InputStream stream, String contentType, long size, String key) {
        provider.putObject(key, stream, size, contentType);

        FileMetadata metadata = FileMetadata.builder()
                .key(key)
                .contentType(contentType)
                .size(size)
                .bucket(properties.getBucketName())
                .uploadDate(LocalDateTime.now())
                .build();

        log.info("Uploaded stream as key '{}'", key);

        return UploadResult.builder()
                .key(key)
                .metadata(metadata)
                .build();
    }

    // ====================== Download ======================

    @Override
    public InputStream download(String key) {
        return provider.getObject(key);
    }

    // ====================== Delete ======================

    @Override
    public void delete(String key) {
        provider.removeObject(key);
        log.info("Deleted file with key '{}'", key);
    }

    // ====================== Utilities ======================

    @Override
    public boolean exists(String key) {
        return provider.objectExists(key);
    }

    @Override
    public List<FileMetadata> list(StorageFolder folder, String entityId) {
        String prefix = folder.getPath() + "/" + entityId + "/";
        List<String> keys = provider.listObjects(prefix);

        return keys.stream()
                .map(k -> FileMetadata.builder()
                        .key(k)
                        .bucket(properties.getBucketName())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public String move(String sourceKey, String destKey) {
        provider.copyObject(sourceKey, destKey);
        provider.removeObject(sourceKey);
        log.info("Moved file from '{}' to '{}'", sourceKey, destKey);
        return destKey;
    }

    @Override
    public String rename(String oldKey, String newKey) {
        return move(oldKey, newKey);
    }

    @Override
    public String generatePresignedUrl(String key, int expiryMinutes) {
        return provider.generatePresignedUrl(key, expiryMinutes);
    }

    // ====================== Validation ======================

    @Override
    public void validateSize(MultipartFile file, long maxSizeBytes) {
        if (file.getSize() > maxSizeBytes) {
            throw new StorageException(String.format(
                    "File size (%d bytes) exceeds the maximum allowed size (%d bytes).",
                    file.getSize(), maxSizeBytes
            ));
        }
    }

    @Override
    public void validateExtension(MultipartFile file, String... allowedExtensions) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new StorageException("File has no name; cannot validate extension.");
        }

        String extension = getExtension(filename).toLowerCase();
        if (extension.isEmpty()) {
            throw new StorageException("File has no extension.");
        }

        // Remove the leading dot for comparison
        String ext = extension.substring(1);
        boolean allowed = Arrays.stream(allowedExtensions)
                .anyMatch(e -> e.equalsIgnoreCase(ext));

        if (!allowed) {
            throw new StorageException(String.format(
                    "File extension '%s' is not allowed. Allowed: %s",
                    ext, Arrays.toString(allowedExtensions)
            ));
        }
    }

    // ====================== Internal helpers ======================

    /**
     * Builds the object key: folder/entityId/filename
     */
    private String buildKey(StorageFolder folder, String entityId, String filename) {
        return folder.getPath() + "/" + entityId + "/" + filename;
    }

    /**
     * Extracts the file extension including the dot (e.g. ".jpg").
     * Returns empty string if no extension is present.
     */
    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex >= 0) ? filename.substring(dotIndex) : "";
    }
}
