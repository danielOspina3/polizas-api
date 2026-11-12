package com.danielospina.polizas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CrearRiesgoRequest(
        @NotBlank(message = "es obligatoria") String direccionInmueble,
        @NotBlank(message = "es obligatorio") String arrendatario,
        @NotBlank(message = "es obligatorio") String arrendador,
        @NotNull(message = "es obligatorio") @Positive(message = "debe ser mayor que cero") BigDecimal canonMensual) {
}
