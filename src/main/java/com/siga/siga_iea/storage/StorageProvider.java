package com.siga.siga_iea.storage;

import java.io.InputStream;
import java.util.List;

/**
 * Low-level abstraction for object storage operations.
 * Implementations can target MinIO, Amazon S3, Azure Blob, local filesystem, etc.
 * The application code never interacts with this interface directly;
 * it goes through {@link StorageService} instead.
 */
public interface StorageProvider {

    /**
     * Stores an object in the bucket.
     *
     * @param key         the object key (e.g. "estudiantes/15/abc123.jpg")
     * @param stream      the data stream
     * @param size        the size in bytes
     * @param contentType the MIME type (e.g. "image/jpeg")
     */
    void putObject(String key, InputStream stream, long size, String contentType);

    /**
     * Retrieves an object from the bucket.
     *
     * @param key the object key
     * @return the data stream
     */
    InputStream getObject(String key);

    /**
     * Deletes an object from the bucket.
     *
     * @param key the object key
     */
    void removeObject(String key);

    /**
     * Copies an object within the same bucket.
     *
     * @param sourceKey the source object key
     * @param destKey   the destination object key
     */
    void copyObject(String sourceKey, String destKey);

    /**
     * Checks if an object exists in the bucket.
     *
     * @param key the object key
     * @return true if the object exists
     */
    boolean objectExists(String key);

    /**
     * Lists all object keys under the given prefix.
     *
     * @param prefix the prefix to filter by (e.g. "estudiantes/15/")
     * @return list of matching object keys
     */
    List<String> listObjects(String prefix);

    /**
     * Generates a temporary presigned URL for direct access to an object.
     *
     * @param key           the object key
     * @param expiryMinutes how long the URL remains valid
     * @return the presigned URL string
     */
    String generatePresignedUrl(String key, int expiryMinutes);

    /**
     * Ensures the configured bucket exists, creating it if necessary.
     */
    void ensureBucketExists();
}
