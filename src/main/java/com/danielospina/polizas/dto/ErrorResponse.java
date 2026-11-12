package com.danielospina.polizas.dto;

import java.time.LocalDateTime;

public record ErrorResponse(String timestamp, int status, String error, String mensaje) {

    public static ErrorResponse of(int status, String error, String mensaje) {
        return new ErrorResponse(LocalDateTime.now().toString(), status, error, mensaje);
    }
}
