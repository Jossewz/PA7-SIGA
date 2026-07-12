package com.siga.siga_iea.storage.exception;

/**
 * Base exception for all storage-related errors.
 */
public class StorageException extends RuntimeException {

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
