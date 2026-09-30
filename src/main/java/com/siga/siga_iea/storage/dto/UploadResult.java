package com.siga.siga_iea.storage.dto;

/**
 * Result of a file upload operation.
 * Contains the storage key (to be persisted in the database) and file metadata.
 */
public class UploadResult {

    /** The object key in the bucket, e.g. "estudiantes/15/a1b2c3d4.jpg" */
    private String key;

    /** Metadata about the uploaded file */
    private FileMetadata metadata;

    public UploadResult() {}

    public UploadResult(String key, FileMetadata metadata) {
        this.key = key;
        this.metadata = metadata;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String key;
        private FileMetadata metadata;

        public Builder key(String key) { this.key = key; return this; }
        public Builder metadata(FileMetadata metadata) { this.metadata = metadata; return this; }

        public UploadResult build() {
            return new UploadResult(key, metadata);
        }
    }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public FileMetadata getMetadata() { return metadata; }
    public void setMetadata(FileMetadata metadata) { this.metadata = metadata; }
}
