package com.siga.siga_iea.storage;

import com.siga.siga_iea.storage.dto.FileMetadata;
import com.siga.siga_iea.storage.dto.UploadResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * High-level facade for storage operations.
 * This is the ONLY interface that domain modules (estudiantes, docentes, etc.)
 * should interact with. They never see MinIO or any provider directly.
 */
public interface StorageService {

    // ====================== Upload ======================

    /**
     * Uploads a file with an auto-generated UUID filename.
     *
     * @param file     the multipart file from the request
     * @param folder   the logical folder (e.g. ESTUDIANTES)
     * @param entityId the entity identifier (e.g. student ID)
     * @return the upload result containing the generated key
     */
    UploadResult upload(MultipartFile file, StorageFolder folder, String entityId);

    /**
     * Uploads raw data with a pre-defined key.
     *
     * @param stream      the input stream
     * @param contentType the MIME type
     * @param size        the size in bytes
     * @param key         the full object key
     * @return the upload result
     */
    UploadResult upload(InputStream stream, String contentType, long size, String key);

    // ====================== Download ======================

    /**
     * Downloads a file by its key.
     *
     * @param key the object key
     * @return the data stream
     */
    InputStream download(String key);

    // ====================== Delete ======================

    /**
     * Deletes a file by its key.
     *
     * @param key the object key
     */
    void delete(String key);

    // ====================== Utilities ======================

    /**
     * Checks if a file exists.
     *
     * @param key the object key
     * @return true if the file exists
     */
    boolean exists(String key);

    /**
     * Lists all files under a folder/entity path.
     *
     * @param folder   the logical folder
     * @param entityId the entity identifier
     * @return list of file metadata
     */
    List<FileMetadata> list(StorageFolder folder, String entityId);

    /**
     * Moves a file from one key to another (copy + delete source).
     *
     * @param sourceKey the current key
     * @param destKey   the new key
     * @return the new key
     */
    String move(String sourceKey, String destKey);

    /**
     * Renames a file (alias for move).
     *
     * @param oldKey the current key
     * @param newKey the new key
     * @return the new key
     */
    String rename(String oldKey, String newKey);

    /**
     * Generates a temporary presigned URL for direct access.
     *
     * @param key           the object key
     * @param expiryMinutes how long the URL is valid
     * @return the presigned URL
     */
    String generatePresignedUrl(String key, int expiryMinutes);

    // ====================== Validation ======================

    /**
     * Validates that a file does not exceed the maximum allowed size.
     *
     * @param file         the file to validate
     * @param maxSizeBytes the maximum allowed size in bytes
     */
    void validateSize(MultipartFile file, long maxSizeBytes);

    /**
     * Validates that a file has one of the allowed extensions.
     *
     * @param file              the file to validate
     * @param allowedExtensions the allowed extensions (e.g. "jpg", "png", "pdf")
     */
    void validateExtension(MultipartFile file, String... allowedExtensions);
}
