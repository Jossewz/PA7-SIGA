package com.siga.siga_iea.shared.validation;

public class ValidationUtil {
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
}

