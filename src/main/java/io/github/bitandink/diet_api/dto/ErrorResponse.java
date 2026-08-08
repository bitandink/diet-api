package io.github.bitandink.diet_api.dto;

import lombok.Getter;

import java.util.Map;

@Getter
public class ErrorResponse {

    private final int status;
    private final String message;
    private final String path;
    private final Map<String, String> errors;

    public ErrorResponse(
            int status,
            String message,
            String path
    ) {
        this.status = status;
        this.message = message;
        this.path = path;
        this.errors = null;
    }

    public ErrorResponse(
            int status,
            String message,
            String path,
            Map<String, String> errors
    ) {
        this.status = status;
        this.message = message;
        this.path = path;
        this.errors = errors;
    }
}