package com.siga.siga_iea.storage;

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

    public String getPath() {
        return path;
    }
}
