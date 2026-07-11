package com.siga.siga_iea.storage;

import lombok.Getter;

/**
 * Enum representing the logical folder structure inside the MinIO bucket.
 * Use this instead of raw strings to avoid typos and keep consistency.
 */
@Getter
public enum StorageFolder {

    ESTUDIANTES("estudiantes"),
    DOCENTES("docentes"),
    MATRICULAS("matriculas"),
    CERTIFICADOS("certificados"),
    BOLETINES("boletines"),
    USUARIOS("usuarios"),
    INSTITUCION("institucion"),
    REPORTES("reportes"),
    TEMP("temp");

    private final String path;

    StorageFolder(String path) {
        this.path = path;
    }
}
