package com.siga.siga_iea.shared.dto;

public class ApiResponseDTO {
    private boolean success;
    private String message;

    public ApiResponseDTO() {}

    public ApiResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}

