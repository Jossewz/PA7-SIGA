package com.siga.siga_iea.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Typed configuration properties for MinIO storage connection.
 * Bound to the "minio" prefix in application.properties.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "minio")
public class StorageProperties {

    private String url;
    private String accessKey;
    private String secretKey;
    private String bucketName;
}
