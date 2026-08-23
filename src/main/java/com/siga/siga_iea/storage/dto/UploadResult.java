package com.siga.siga_iea.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Result of a file upload operation.
 * Contains the storage key (to be persisted in the database) and file metadata.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResult {

    /** The object key in the bucket, e.g. "estudiantes/15/a1b2c3d4.jpg" */
    private String key;

    /** Metadata about the uploaded file */
    private FileMetadata metadata;
}
