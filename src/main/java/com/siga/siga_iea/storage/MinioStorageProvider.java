package com.siga.siga_iea.storage;

import com.siga.siga_iea.storage.exception.StorageException;
import com.siga.siga_iea.storage.exception.StorageFileNotFoundException;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * MinIO-specific implementation of {@link StorageProvider}.
 * Uses the MinIO Java SDK to interact with the configured bucket.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MinioStorageProvider implements StorageProvider {

    private final MinioClient minioClient;
    private final StorageProperties properties;

    /**
     * On application startup, ensure the bucket exists.
     * If MinIO is unreachable (e.g. during tests or standalone builds),
     * log a warning and continue without breaking the Spring context.
     */
    @PostConstruct
    public void init() {
        try {
            ensureBucketExists();
            log.info("MinIO bucket '{}' is ready.", properties.getBucketName());
        } catch (Exception e) {
            log.warn("Could not initialize MinIO bucket '{}'. "
                    + "Storage operations will fail until MinIO is available. Error: {}",
                    properties.getBucketName(), e.getMessage());
        }
    }

    @Override
    public void putObject(String key, InputStream stream, long size, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(key)
                            .stream(stream, size, -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception e) {
            throw new StorageException("Failed to upload object: " + key, e);
        }
    }

    @Override
    public InputStream getObject(String key) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(key)
                            .build()
            );
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                throw new StorageFileNotFoundException("File not found: " + key, e);
            }
            throw new StorageException("Failed to download object: " + key, e);
        } catch (Exception e) {
            throw new StorageException("Failed to download object: " + key, e);
        }
    }

    @Override
    public void removeObject(String key) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(key)
                            .build()
            );
        } catch (Exception e) {
            throw new StorageException("Failed to delete object: " + key, e);
        }
    }

    @Override
    public void copyObject(String sourceKey, String destKey) {
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(destKey)
                            .source(CopySource.builder()
                                    .bucket(properties.getBucketName())
                                    .object(sourceKey)
                                    .build())
                            .build()
            );
        } catch (Exception e) {
            throw new StorageException(
                    "Failed to copy object from '" + sourceKey + "' to '" + destKey + "'", e);
        }
    }

    @Override
    public boolean objectExists(String key) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(key)
                            .build()
            );
            return true;
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                return false;
            }
            throw new StorageException("Failed to check existence of object: " + key, e);
        } catch (Exception e) {
            throw new StorageException("Failed to check existence of object: " + key, e);
        }
    }

    @Override
    public List<String> listObjects(String prefix) {
        try {
            List<String> keys = new ArrayList<>();
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(properties.getBucketName())
                            .prefix(prefix)
                            .recursive(true)
                            .build()
            );
            for (Result<Item> result : results) {
                keys.add(result.get().objectName());
            }
            return keys;
        } catch (Exception e) {
            throw new StorageException("Failed to list objects with prefix: " + prefix, e);
        }
    }

    @Override
    public String generatePresignedUrl(String key, int expiryMinutes) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(properties.getBucketName())
                            .object(key)
                            .expiry(expiryMinutes, TimeUnit.MINUTES)
                            .build()
            );
        } catch (Exception e) {
            throw new StorageException("Failed to generate presigned URL for: " + key, e);
        }
    }

    @Override
    public void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(properties.getBucketName())
                            .build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(properties.getBucketName())
                                .build()
                );
                log.info("Created MinIO bucket: {}", properties.getBucketName());
            }
        } catch (Exception e) {
            throw new StorageException("Failed to ensure bucket exists: " + properties.getBucketName(), e);
        }
    }
}
