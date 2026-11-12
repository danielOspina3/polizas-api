package com.danielospina.polizas.exception;

/** Se lanza cuando la póliza o el riesgo solicitado no existe (HTTP 404). */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
