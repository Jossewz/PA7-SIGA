package com.siga.siga_iea.storage.dto;

import java.time.LocalDateTime;

/**
 * DTO containing metadata about a stored file.
 * Used internally by the storage module and returned by list operations.
 */
public class FileMetadata {

    private String key;
    private String originalFilename;
    private String contentType;
    private long size;
    private String bucket;
    private String etag;
    private LocalDateTime uploadDate;

    public FileMetadata() {}

    public FileMetadata(String key, String originalFilename, String contentType, long size, String bucket, String etag, LocalDateTime uploadDate) {
        this.key = key;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.size = size;
        this.bucket = bucket;
        this.etag = etag;
        this.uploadDate = uploadDate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String key;
        private String originalFilename;
        private String contentType;
        private long size;
        private String bucket;
        private String etag;
        private LocalDateTime uploadDate;

        public Builder key(String key) { this.key = key; return this; }
        public Builder originalFilename(String originalFilename) { this.originalFilename = originalFilename; return this; }
        public Builder contentType(String contentType) { this.contentType = contentType; return this; }
        public Builder size(long size) { this.size = size; return this; }
        public Builder bucket(String bucket) { this.bucket = bucket; return this; }
        public Builder etag(String etag) { this.etag = etag; return this; }
        public Builder uploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; return this; }

        public FileMetadata build() {
            return new FileMetadata(key, originalFilename, contentType, size, bucket, etag, uploadDate);
        }
    }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public String getBucket() { return bucket; }
    public void setBucket(String bucket) { this.bucket = bucket; }

    public String getEtag() { return etag; }
    public void setEtag(String etag) { this.etag = etag; }

    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }
}
