package com.danielospina.polizas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Cuerpo del evento que se envía al CORE: {"evento": "ACTUALIZACION", "polizaId": 555}. */
public record CoreEventoRequest(
        @NotBlank(message = "es obligatorio") String evento,
        @NotNull(message = "es obligatorio") Long polizaId) {
}
