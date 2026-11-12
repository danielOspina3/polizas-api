package com.danielospina.polizas.exception;

import com.danielospina.polizas.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/** Traduce las excepciones a respuestas HTTP con un formato de error uniforme. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final int UNPROCESSABLE_ENTITY = 422;

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> manejarNoEncontrado(RecursoNoEncontradoException ex) {
        return construir(HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> manejarReglaNegocio(ReglaNegocioException ex) {
        return construir(UNPROCESSABLE_ENTITY, "Unprocessable Entity", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidacion(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return construir(HttpStatus.BAD_REQUEST.value(), "Bad Request", mensaje);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> manejarTipoInvalido(MethodArgumentTypeMismatchException ex) {
        String mensaje = "Valor inválido para el parámetro '" + ex.getName() + "': " + ex.getValue();
        return construir(HttpStatus.BAD_REQUEST.value(), "Bad Request", mensaje);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> manejarCuerpoInvalido(HttpMessageNotReadableException ex) {
        return construir(HttpStatus.BAD_REQUEST.value(), "Bad Request", "El cuerpo de la petición no es válido");
    }

    private ResponseEntity<ErrorResponse> construir(int status, String error, String mensaje) {
        return ResponseEntity.status(status).body(ErrorResponse.of(status, error, mensaje));
    }
}
