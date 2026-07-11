package com.siga.siga_iea.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO containing metadata about a stored file.
 * Used internally by the storage module and returned by list operations.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadata {

    private String key;
    private String originalFilename;
    private String contentType;
    private long size;
    private String bucket;
    private String etag;
    private LocalDateTime uploadDate;
}
