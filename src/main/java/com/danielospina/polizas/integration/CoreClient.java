package com.danielospina.polizas.integration;

/**
 * Puerto de salida hacia el CORE de seguros. El servicio de negocio solo conoce
 * esta interfaz; la implementación concreta (hoy un mock HTTP) se puede cambiar
 * por el cliente real del servicio de edición en WebLogic sin tocar el negocio.
 */
public interface CoreClient {

    void enviarEvento(String evento, Long polizaId);
}
