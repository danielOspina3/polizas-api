package com.danielospina.polizas.integration;

/** Evento interno que se publica cada vez que una operación modifica una póliza o un riesgo. */
public record OperacionPolizaEvent(String evento, Long polizaId) {
}
