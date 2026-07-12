package com.siga.siga_iea.storage.exception;

/**
 * Thrown when a requested file does not exist in storage.
 */
public class StorageFileNotFoundException extends StorageException {

    public StorageFileNotFoundException(String message) {
        super(message);
    }

    public StorageFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
