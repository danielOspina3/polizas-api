package com.danielospina.polizas.integration;

import com.danielospina.polizas.config.ApiKeyFilter;
import com.danielospina.polizas.dto.CoreEventoRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Envía cada operación al endpoint del CORE (en esta prueba, el mock /core-mock/evento). */
@Component
public class CoreHttpClient implements CoreClient {

    private static final Logger log = LoggerFactory.getLogger(CoreHttpClient.class);

    private final RestClient restClient;
    private final String coreUrl;

    public CoreHttpClient(@Value("${app.core.url}") String coreUrl,
                          @Value("${app.seguridad.api-key}") String apiKey) {
        this.coreUrl = coreUrl;
        this.restClient = RestClient.builder()
                .defaultHeader(ApiKeyFilter.HEADER_API_KEY, apiKey)
                .build();
    }

    @Override
    public void enviarEvento(String evento, Long polizaId) {
        try {
            restClient.post()
                    .uri(coreUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new CoreEventoRequest(evento, polizaId))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Evento {} de la póliza {} enviado al CORE", evento, polizaId);
        } catch (RestClientException ex) {
            // La operación ya quedó guardada: un fallo del CORE no la revierte, solo se registra
            log.error("No fue posible enviar el evento {} de la póliza {} al CORE: {}", evento, polizaId, ex.getMessage());
        }
    }
}
